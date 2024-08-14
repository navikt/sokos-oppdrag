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
        fagsystemId: String,
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
                          ELSE TRIM(OE.ENHET) END                             AS ANSVARSSTED
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
            if (fagsystemId.isNotBlank()) statementParts.add("AND O.FAGSYSTEM_ID = :FAGSYSTEMID")
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
            if (gjelderId.isNotBlank() || fagsystemId.isNotBlank()) statementParts.add("OPTIMIZE FOR 1 ROW")
            session.list(
                queryOf(
                    statementParts.joinToString("\n", "", ";"),
                    mapOf(
                        "GJELDERID" to gjelderId,
                        "FAGSYSTEMID" to fagsystemId,
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
                    select TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    from T_FAGOMRAADE
                    """.trimIndent(),
                ),
                mapToFagOmraade,
            )
        }
    }

    fun getOppdragsDetaljer(oppdragsIder: List<Int>): List<OppdragsDetaljer> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                         SELECT O.OPPDRAGS_ID                                       AS OPPDRAGS_ID
                         , F.ANT_ATTESTANTER                                        AS ANT_ATTESTANTER
                         , L.LINJE_ID                                               AS LINJE_ID
                         , O.OPPDRAG_GJELDER_ID                                     AS OPPDRAG_GJELDER_ID
                         , TRIM(G.NAVN_FAGGRUPPE)                                   AS NAVN_FAGGRUPPE
                         , TRIM(F.NAVN_FAGOMRAADE)                                  AS NAVN_FAGOMRAADE
                         , O.KODE_FAGOMRAADE                                        AS KODE_FAGOMRAADE
                         , TRIM(O.FAGSYSTEM_ID)                                     AS FAGSYSTEM_ID
                         , TRIM(L.KODE_KLASSE)                                      AS KODE_KLASSE
                         , TRIM(L.DELYTELSE_ID)                                     AS DELYTELSE_ID
                         , L.SATS                                                   AS SATS
                         , TRIM(L.TYPE_SATS)                                        AS TYPE_SATS
                         , L.DATO_VEDTAK_FOM                                        AS DATO_VEDTAK_FOM
                         , COALESCE(L2.DATO_VEDTAK_FOM - 1 DAY, L.DATO_VEDTAK_TOM)  AS DATO_VEDTAK_TOM
                         , TRIM(A.ATTESTANT_ID)                                     AS ATTESTANT_ID
                         , A.DATO_UGYLDIG_FOM                                       AS DATO_UGYLDIG_FOM
                         , TRIM(OE.ENHET)                                           AS KOSTNADSSTED
                         , CASE WHEN LE.ENHET IS NOT NULL THEN TRIM(LE.ENHET)
                                WHEN OEB.ENHET IS NOT NULL THEN TRIM(OEB.ENHET)
                                ELSE TRIM(OE.ENHET)
                        END                                                         AS ANSVARSSTED
                    FROM T_FAGGRUPPE G
                             JOIN T_FAGOMRAADE F ON G.KODE_FAGGRUPPE = F.KODE_FAGGRUPPE
                             JOIN T_OPPDRAG O ON F.KODE_FAGOMRAADE = O.KODE_FAGOMRAADE
                             JOIN T_OPPDRAGSLINJE L ON O.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_OPPDRAG_STATUS S ON S.OPPDRAGS_ID = L.OPPDRAGS_ID
                             JOIN T_LINJE_STATUS LS ON LS.OPPDRAGS_ID = L.OPPDRAGS_ID AND LS.LINJE_ID = L.LINJE_ID
                             JOIN T_OPPDRAGSENHET OE ON OE.OPPDRAGS_ID = O.OPPDRAGS_ID AND OE.TYPE_ENHET = 'BOS'
                             JOIN T_STATUSKODE SK ON SK.KODE_STATUS = LS.KODE_STATUS
                             LEFT JOIN T_KORREKSJON K ON L.OPPDRAGS_ID = K.OPPDRAGS_ID AND L.LINJE_ID = K.LINJE_ID
                             LEFT JOIN T_OPPDRAG O2 ON K.OPPDRAGS_ID_KORR = O2.OPPDRAGS_ID AND F.KODE_FAGOMRAADE = O2.KODE_FAGOMRAADE
                             LEFT JOIN T_OPPDRAGSLINJE L2 ON L2.OPPDRAGS_ID = O2.OPPDRAGS_ID AND K.LINJE_ID_KORR = L2.LINJE_ID AND L.DATO_VEDTAK_FOM < L2.DATO_VEDTAK_FOM
                             LEFT JOIN T_ATTESTASJON A ON A.OPPDRAGS_ID = L.OPPDRAGS_ID AND A.LINJE_ID = L.LINJE_ID AND A.DATO_UGYLDIG_FOM > CURRENT DATE
                             LEFT OUTER JOIN T_LINJEENHET LE ON LE.OPPDRAGS_ID = L.OPPDRAGS_ID AND LE.TYPE_ENHET = 'BEH' AND LE.LINJE_ID = L.LINJE_ID
                             LEFT OUTER JOIN T_OPPDRAGSENHET OEB ON OEB.OPPDRAGS_ID = O.OPPDRAGS_ID AND OEB.TYPE_ENHET = 'BEH'
                    WHERE S.KODE_STATUS = 'AKTI'
                      AND NOT (L2.DATO_VEDTAK_FOM IS NULL AND SK.TYPE_STATUS != 'AKTI')
                      AND S.TIDSPKT_REG = (SELECT MAX(S2.TIDSPKT_REG)
                                           FROM T_OPPDRAG_STATUS S2
                                           WHERE S.OPPDRAGS_ID = S2.OPPDRAGS_ID)
                      AND LS.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                            FROM T_LINJE_STATUS LS2
                                            WHERE LS2.OPPDRAGS_ID = LS.OPPDRAGS_ID
                                              AND LS2.LINJE_ID = LS.LINJE_ID)
                      AND (A.OPPDRAGS_ID IS NULL OR A.LOPENR = (SELECT MAX(A2.LOPENR)
                                                                FROM T_ATTESTASJON A2
                                                                WHERE A2.OPPDRAGS_ID = L.OPPDRAGS_ID
                                                                  AND A2.LINJE_ID = L.LINJE_ID
                                                                  AND A2.ATTESTANT_ID = A.ATTESTANT_ID))
                      AND OE.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                            FROM T_OPPDRAGSENHET OE2
                                            WHERE OE2.OPPDRAGS_ID = OE.OPPDRAGS_ID
                                              AND OE2.TYPE_ENHET  = OE.TYPE_ENHET
                                              AND OE2.DATO_FOM   <= CURRENT DATE)
                      AND (LE.TIDSPKT_REG IS NULL OR LE.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                                                       FROM T_LINJEENHET LE2
                                                                       WHERE LE2.OPPDRAGS_ID = LE.OPPDRAGS_ID
                                                                         AND LE2.LINJE_ID    = LE.LINJE_ID
                                                                         AND LE2.TYPE_ENHET  = LE.TYPE_ENHET
                                                                         AND LE2.DATO_FOM   <= CURRENT DATE))
                      AND (OEB.TIDSPKT_REG IS NULL OR OEB.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                                                         FROM T_OPPDRAGSENHET OEB2
                                                                         WHERE OEB2.OPPDRAGS_ID = OEB.OPPDRAGS_ID
                                                                           AND OEB2.TYPE_ENHET  = OEB.TYPE_ENHET
                                                                           AND OEB2.DATO_FOM   <= CURRENT DATE))
                      AND O.OPPDRAGS_ID  IN (${oppdragsIder.joinToString()})
                    ORDER BY OPPDRAGS_ID, LINJE_ID
                    """.trimIndent(),
                ),
                mapToOppdragslinjerTilAttestasjon,
            )
        }
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarsSted = row.string("ANSVARSSTED"),
            fagsystemId = row.string("FAGSYSTEM_ID"),
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
            kostnadsSted = row.string("KOSTNADSSTED"),
            navnFagGruppe = row.string("NAVN_FAGGRUPPE"),
            navnFagOmraade = row.string("NAVN_FAGOMRAADE"),
            oppdragsId = row.int("OPPDRAGS_ID"),
        )
    }

    private val mapToOppdragslinjerTilAttestasjon: (Row) -> OppdragsDetaljer = { row ->
        OppdragsDetaljer(
            ansvarsSted = row.string("ANSVARSSTED"),
            antallAttestanter = row.int("ANT_ATTESTANTER"),
            attestant = row.stringOrNull("ATTESTANT_ID"),
            datoUgyldigFom = row.stringOrNull("DATO_UGYLDIG_FOM"),
            datoVedtakFom = row.string("DATO_VEDTAK_FOM"),
            datoVedtakTom = row.stringOrNull("DATO_VEDTAK_TOM"),
            delytelsesId = row.string("DELYTELSE_ID"),
            navnFagGruppe = row.string("NAVN_FAGGRUPPE"),
            navnFagOmraade = row.string("NAVN_FAGOMRAADE"),
            fagSystemId = row.string("FAGSYSTEM_ID"),
            klasse = row.string("KODE_KLASSE"),
            kostnadsSted = row.string("KOSTNADSSTED"),
            linjeId = row.string("LINJE_ID"),
            oppdragGjelderId = row.string("OPPDRAG_GJELDER_ID"),
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
