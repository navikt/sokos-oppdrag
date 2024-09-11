package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragslinjeWithoutFluff
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
                    SELECT TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    FROM T_FAGOMRAADE
                    """.trimIndent(),
                ),
                mapToFagOmraade,
            )
        }
    }

    fun getOppdragslinjerWithoutFluff(oppdragsId: Int): List<OppdragslinjeWithoutFluff> {
        val query =
            """
            select l.OPPDRAGS_ID, l.LINJE_ID, l.KODE_KLASSE, DATO_VEDTAK_FOM, DATO_VEDTAK_TOM, ATTESTERT, l.SATS, l.TYPE_SATS, l.DELYTELSE_ID
            from T_OPPDRAGSLINJE l
                     join T_LINJE_STATUS statusNy on statusNy.LINJE_ID = l.LINJE_ID and statusNy.OPPDRAGS_ID = l.OPPDRAGS_ID
            where statusNy.KODE_STATUS = 'NY'
              and not exists(select korrannuopph.TIDSPKT_REG
                             from T_LINJE_STATUS korrannuopph
                             where korrannuopph.LINJE_ID = l.LINJE_ID
                               and korrannuopph.OPPDRAGS_ID = l.OPPDRAGS_ID
                               and korrannuopph.KODE_STATUS in ('KORR', 'ANNU', 'OPPH')
                               and korrannuopph.DATO_FOM = statusNy.DATO_FOM
                               and not exists(select 1
                                              from T_LINJE_STATUS andreStatuser
                                              where andreStatuser.LINJE_ID = l.LINJE_ID
                                                and andreStatuser.OPPDRAGS_ID = l.OPPDRAGS_ID
                                                and andreStatuser.KODE_STATUS in ('IKAT', 'ATTE', 'HVIL', 'REAK', 'FBER', 'LOPE')
                                                and andreStatuser.DATO_FOM >= statusNy.DATO_FOM
                                                and andreStatuser.TIDSPKT_REG > korrannuopph.TIDSPKT_REG))
              and l.OPPDRAGS_ID = :OPPDRAGSID
            order by KODE_KLASSE, LINJE_ID, DATO_VEDTAK_FOM;
            """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    query,
                    mapOf(
                        "OPPDRAGSID" to oppdragsId,
                    ),
                ),
                mapToOppdragslinjerWithoutFluff,
            )
        }
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarsSted = row.stringOrNull("ANSVARSSTED"),
            fagsystemId = row.string("FAGSYSTEM_ID"),
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
            kostnadsSted = row.string("KOSTNADSSTED"),
            fagGruppe = row.string("NAVN_FAGGRUPPE"),
            fagOmraade = row.string("NAVN_FAGOMRAADE"),
            oppdragsId = row.int("OPPDRAGS_ID"),
        )
    }

    private val mapToOppdragslinjerWithoutFluff: (Row) -> OppdragslinjeWithoutFluff = { row ->
        OppdragslinjeWithoutFluff(
            oppdragsId = row.int("OPPDRAGS_ID"),
            linjeId = row.int("LINJE_ID"),
            kodeKlasse = row.string("KODE_KLASSE"),
            datoVedtakFom = row.localDate("DATO_VEDTAK_FOM"),
            datoVedtakTom = row.localDate("DATO_VEDTAK_TOM"),
            attestert = row.boolean("ATTESTERT"),
            sats = row.double("SATS"),
            typeSats = row.string("TYPE_SATS"),
            delytelseId = row.int("DELYTELSE_ID"),
        )
    }

    private val mapToFagOmraade: (Row) -> FagOmraade = { row ->
        FagOmraade(
            navn = row.string("NAVN_FAGOMRAADE"),
            kode = row.string("KODE_FAGOMRAADE"),
        )
    }
}
