package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun getOppdrag(
        gjelderId: String,
        fagSystemId: String,
        kodeFaggruppe: String,
        kodeFagomraade: String,
        attestert: Boolean?,
    ): List<Oppdrag> {
        return using(sessionOf(dataSource)) { session ->
            val statementParts =
                mutableListOf(
                    """
                    SELECT TRIM(G.NAVN_FAGGRUPPE)                             AS NAVN_FAGGRUPPE
                         , TRIM(F.NAVN_FAGOMRAADE)                            AS NAVN_FAGOMRAADE
                         , O.OPPDRAG_GJELDER_ID                               AS OPPDRAG_GJELDER_ID
                         , O.OPPDRAGS_ID                                      AS OPPDRAGS_ID
                         , TRIM(O.FAGSYSTEM_ID)                               AS FAGSYSTEM_ID
                         , LS.KODE_STATUS                                     AS KODE_STATUS
                         , OE.ENHET                                           AS KOSTNADSSTED
                         , CASE WHEN LE.ENHET IS NOT NULL THEN TRIM(LE.ENHET)
                          WHEN OEB.ENHET IS NOT NULL THEN TRIM(OEB.ENHET)
                          ELSE NULL END                                       AS ANSVARSSTED
                    FROM T_FAGGRUPPE G
                             JOIN T_FAGOMRAADE F ON G.KODE_FAGGRUPPE = F.KODE_FAGGRUPPE
                             JOIN T_OPPDRAG O ON F.KODE_FAGOMRAADE = O.KODE_FAGOMRAADE
                             JOIN T_OPPDRAGSLINJE L ON O.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_OPPDRAGSENHET OE ON OE.OPPDRAGS_ID = O.OPPDRAGS_ID AND OE.TYPE_ENHET = 'BOS'
                             JOIN T_OPPDRAG_STATUS S ON S.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_LINJE_STATUS LS ON LS.OPPDRAGS_ID = L.OPPDRAGS_ID AND LS.LINJE_ID = L.LINJE_ID
                             LEFT OUTER JOIN T_KORREKSJON K ON L.OPPDRAGS_ID = K.OPPDRAGS_ID AND L.LINJE_ID = K.LINJE_ID
                             LEFT OUTER JOIN T_LINJEENHET LE ON LE.OPPDRAGS_ID = L.OPPDRAGS_ID AND LE.TYPE_ENHET = 'BEH' AND LE.LINJE_ID = L.LINJE_ID
                             LEFT OUTER JOIN T_OPPDRAGSENHET OEB ON OEB.OPPDRAGS_ID = O.OPPDRAGS_ID AND OEB.TYPE_ENHET = 'BEH'
                    WHERE K.OPPDRAGS_ID IS NULL
                      AND S.KODE_STATUS = 'AKTI'
                      AND S.TIDSPKT_REG = (SELECT MAX(S2.TIDSPKT_REG)
                                           FROM T_OPPDRAG_STATUS S2
                                           WHERE S.OPPDRAGS_ID = S2.OPPDRAGS_ID)
                      AND LS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                            FROM T_LINJE_STATUS LS2
                                            WHERE LS2.OPPDRAGS_ID = LS.OPPDRAGS_ID
                                              AND LS2.LINJE_ID = LS.LINJE_ID)
                    """.trimIndent(),
                )
            if (gjelderId.isNotBlank()) statementParts.add("AND O.OPPDRAG_GJELDER_ID = :GJELDERID")
            if (fagSystemId.isNotBlank()) statementParts.add("AND O.FAGSYSTEM_ID = :FAGSYSTEMID")
            if (kodeFagomraade.isNotBlank()) statementParts.add("AND F.KODE_FAGOMRAADE = :KODEFAGOMRAADE")
            if (kodeFaggruppe.isNotBlank()) statementParts.add("AND G.KODE_FAGGRUPPE = :KODEFAGGRUPPE")

            if (attestert == false) {
                statementParts.add("AND L.ATTESTERT = 'N'")
            } else if (attestert == true) {
                statementParts.add("AND L.ATTESTERT = 'J'")
            } else if (attestert == null) {
                statementParts.add("AND L.ATTESTERT LIKE '%'")
            }

            statementParts.add("GROUP BY NAVN_FAGGRUPPE, NAVN_FAGOMRAADE, OPPDRAG_GJELDER_ID, O.OPPDRAGS_ID, FAGSYSTEM_ID, LS.KODE_STATUS, OE.ENHET, LE.ENHET, OEB.ENHET")
            statementParts.add("FETCH FIRST 200 ROWS ONLY")
            if (gjelderId.isNotBlank() || fagSystemId.isNotBlank()) statementParts.add("OPTIMIZE FOR 1 ROW")
            session.list(
                queryOf(
                    statementParts.joinToString("\n", "", ";"),
                    mapOf(
                        "GJELDERID" to gjelderId,
                        "FAGSYSTEMID" to fagSystemId,
                        "KODEFAGOMRAADE" to kodeFagomraade,
                        "KODEFAGGRUPPE" to kodeFaggruppe,
                        "ATTESTERT" to attestert,
                    ),
                ),
                mapToOppdrag,
            )
        }
    }

    fun getFagOmraader(): List<FagOmraade> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    FROM T_FAGOMRAADE
                    """.trimIndent(),
                ),
                mapToFagOmraade,
            )
        }
    }

    fun getOppdragsDetaljer(oppdragsId: Int): List<OppdragsDetaljer> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT O.OPPDRAGS_ID                                            AS OPPDRAGS_ID
                         , F.ANT_ATTESTANTER                                        AS ANT_ATTESTANTER
                         , L.LINJE_ID                                               AS LINJE_ID
                         , O.OPPDRAG_GJELDER_ID                                     AS OPPDRAG_GJELDER_ID
                         , TRIM(G.NAVN_FAGGRUPPE)                                   AS NAVN_FAGGRUPPE
                         , TRIM(F.NAVN_FAGOMRAADE)                                  AS NAVN_FAGOMRAADE
                         , TRIM(O.KODE_FAGOMRAADE)                                  AS KODE_FAGOMRAADE
                         , TRIM(O.FAGSYSTEM_ID)                                     AS FAGSYSTEM_ID
                         , TRIM(L.KODE_KLASSE)                                      AS KODE_KLASSE
                         , TRIM(L.DELYTELSE_ID)                                     AS DELYTELSE_ID
                         , L.SATS                                                   AS SATS
                         , TRIM(L.TYPE_SATS)                                        AS TYPE_SATS
                         , L.DATO_VEDTAK_FOM                                        AS DATO_VEDTAK_FOM
                         , COALESCE(L2.DATO_VEDTAK_FOM - 1 DAY, L.DATO_VEDTAK_TOM)  AS DATO_VEDTAK_TOM
                         , TRIM(A.ATTESTANT_ID)                                     AS ATTESTANT_ID
                         , A.DATO_UGYLDIG_FOM                                       AS DATO_UGYLDIG_FOM
                         , TRIM(OKS.ENHET)                                          AS KOSTNADSSTEDFOROPPDRAG
                         , TRIM(OAS.ENHET)                                          AS ANSVARSSTEDFOROPPDRAG
                         , TRIM(LKS.ENHET)                                          AS KOSTNADSSTEDFORLINJE
                         , TRIM(LAS.ENHET)                                          AS ANSVARSSTEDFORLINJE
                    FROM T_FAGGRUPPE G
                             JOIN T_FAGOMRAADE F ON G.KODE_FAGGRUPPE = F.KODE_FAGGRUPPE
                             JOIN T_OPPDRAG O ON F.KODE_FAGOMRAADE = O.KODE_FAGOMRAADE
                             JOIN T_OPPDRAGSLINJE L ON O.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_OPPDRAG_STATUS S ON S.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_LINJE_STATUS LS ON LS.OPPDRAGS_ID = L.OPPDRAGS_ID AND LS.LINJE_ID = L.LINJE_ID
                             JOIN T_OPPDRAGSENHET OKS ON OKS.OPPDRAGS_ID = O.OPPDRAGS_ID AND OKS.TYPE_ENHET = 'BOS'
                             JOIN T_STATUSKODE SK ON SK.KODE_STATUS = LS.KODE_STATUS
                             LEFT JOIN T_KORREKSJON K ON L.OPPDRAGS_ID = K.OPPDRAGS_ID AND L.LINJE_ID = K.LINJE_ID
                             LEFT JOIN T_OPPDRAG O2 ON K.OPPDRAGS_ID_KORR = O2.OPPDRAGS_ID AND F.KODE_FAGOMRAADE = O2.KODE_FAGOMRAADE
                             LEFT JOIN T_OPPDRAGSLINJE L2 ON L2.OPPDRAGS_ID = O2.OPPDRAGS_ID AND K.LINJE_ID_KORR = L2.LINJE_ID AND L.DATO_VEDTAK_FOM < L2.DATO_VEDTAK_FOM
                             LEFT JOIN T_ATTESTASJON A ON A.OPPDRAGS_ID = L.OPPDRAGS_ID AND A.LINJE_ID = L.LINJE_ID AND A.DATO_UGYLDIG_FOM > CURRENT DATE AND L.ATTESTERT = 'J'
                             LEFT OUTER JOIN T_LINJEENHET LKS ON LKS.OPPDRAGS_ID = L.OPPDRAGS_ID AND LKS.LINJE_ID = L.LINJE_ID AND LKS.TYPE_ENHET = 'BOS' 
                             LEFT OUTER JOIN T_LINJEENHET LAS ON LAS.OPPDRAGS_ID = L.OPPDRAGS_ID AND LAS.LINJE_ID = L.LINJE_ID AND LAS.TYPE_ENHET = 'BEH' 
                             LEFT OUTER JOIN T_OPPDRAGSENHET OAS ON OAS.OPPDRAGS_ID = O.OPPDRAGS_ID AND OAS.TYPE_ENHET = 'BEH'
                    WHERE S.KODE_STATUS = 'AKTI'
                      AND NOT (L2.DATO_VEDTAK_FOM IS NULL AND SK.TYPE_STATUS != 'AKTI')
                      AND S.TIDSPKT_REG = (SELECT MAX(S2.TIDSPKT_REG)
                                           FROM T_OPPDRAG_STATUS S2
                                           WHERE S.OPPDRAGS_ID = S2.OPPDRAGS_ID)
                      AND LS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                            FROM T_LINJE_STATUS LS2
                                            WHERE LS2.OPPDRAGS_ID = LS.OPPDRAGS_ID
                                              AND LS2.LINJE_ID = LS.LINJE_ID)
                      AND (A.OPPDRAGS_ID IS NULL OR A.LOPENR =          (SELECT MAX(A2.LOPENR)
                                                                          FROM T_ATTESTASJON A2
                                                                          WHERE A2.OPPDRAGS_ID = L.OPPDRAGS_ID
                                                                          AND A2.LINJE_ID = L.LINJE_ID
                                                                          AND L.ATTESTERT = 'J'
                                                                          AND A2.ATTESTANT_ID = A.ATTESTANT_ID))
                      AND OKS.TIDSPKT_REG =                             (SELECT MAX(TIDSPKT_REG)
                                                                         FROM T_OPPDRAGSENHET OKS2
                                                                         WHERE OKS2.OPPDRAGS_ID = OKS.OPPDRAGS_ID
                                                                           AND OKS2.TYPE_ENHET  = OKS.TYPE_ENHET
                                                                           AND OKS2.DATO_FOM   <= CURRENT DATE)
                      AND (LKS.TIDSPKT_REG IS NULL OR LKS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                                                         FROM T_LINJEENHET LKS2
                                                                         WHERE LKS2.OPPDRAGS_ID = LKS.OPPDRAGS_ID
                                                                           AND LKS2.LINJE_ID    = LKS.LINJE_ID
                                                                           AND LKS2.TYPE_ENHET  = LKS.TYPE_ENHET
                                                                           AND LKS2.DATO_FOM   <= CURRENT DATE))
                      AND (LAS.TIDSPKT_REG IS NULL OR LAS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                                                         FROM T_LINJEENHET LAS2
                                                                         WHERE LAS2.OPPDRAGS_ID = LAS.OPPDRAGS_ID
                                                                           AND LAS2.LINJE_ID    = LAS.LINJE_ID
                                                                           AND LAS2.TYPE_ENHET  = LAS.TYPE_ENHET
                                                                           AND LAS2.DATO_FOM   <= CURRENT DATE))
                      AND (OAS.TIDSPKT_REG IS NULL OR OAS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                                                         FROM T_OPPDRAGSENHET OAS2
                                                                         WHERE OAS2.OPPDRAGS_ID = OAS.OPPDRAGS_ID
                                                                           AND OAS2.TYPE_ENHET  = OAS.TYPE_ENHET
                                                                           AND OAS2.DATO_FOM   <= CURRENT DATE))
                      AND O.OPPDRAGS_ID = :OPPDRAGSID
                    ORDER BY OPPDRAGS_ID, LINJE_ID
                    """.trimIndent(),
                    mapOf(
                        "OPPDRAGSID" to oppdragsId,
                    ),
                ),
                mapToOppdragsDetaljer,
            )
        }
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarsSted = row.stringOrNull("ANSVARSSTED"),
            fagSystemId = row.string("FAGSYSTEM_ID"),
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
            kostnadsSted = row.string("KOSTNADSSTED"),
            fagGruppe = row.string("NAVN_FAGGRUPPE"),
            fagOmraade = row.string("NAVN_FAGOMRAADE"),
            oppdragsId = row.int("OPPDRAGS_ID"),
        )
    }

    private val mapToOppdragsDetaljer: (Row) -> OppdragsDetaljer = { row ->
        OppdragsDetaljer(
            ansvarsStedForOppdrag = row.stringOrNull("ANSVARSSTEDFOROPPDRAG"),
            ansvarsStedForOppdragsLinje = row.stringOrNull("ANSVARSSTEDFORLINJE"),
            antallAttestanter = row.int("ANT_ATTESTANTER"),
            attestant = row.stringOrNull("ATTESTANT_ID"),
            datoUgyldigFom = row.stringOrNull("DATO_UGYLDIG_FOM"),
            datoVedtakFom = row.string("DATO_VEDTAK_FOM"),
            datoVedtakTom = row.stringOrNull("DATO_VEDTAK_TOM"),
            delytelsesId = row.string("DELYTELSE_ID"),
            fagSystemId = row.string("FAGSYSTEM_ID"),
            kodeFagOmraade = row.string("KODE_FAGOMRAADE"),
            kodeKlasse = row.string("KODE_KLASSE"),
            kostnadsStedForOppdrag = row.string("KOSTNADSSTEDFOROPPDRAG"),
            kostnadsStedForOppdragsLinje = row.stringOrNull("KOSTNADSSTEDFORLINJE"),
            linjeId = row.string("LINJE_ID"),
            fagGruppe = row.string("NAVN_FAGGRUPPE"),
            fagOmraade = row.string("NAVN_FAGOMRAADE"),
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
            oppdragsId = row.string("OPPDRAGS_ID"),
            sats = row.double("SATS"),
            satstype = row.string("TYPE_SATS"),
        )
    }

    private val mapToFagOmraade: (Row) -> FagOmraade = { row ->
        FagOmraade(
            navn = row.string("NAVN_FAGOMRAADE"),
            kode = row.string("KODE_FAGOMRAADE"),
        )
    }
}
