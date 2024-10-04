package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun getOppdrag(
        attestert: Boolean?,
        fagSystemId: String?,
        gjelderId: String?,
        kodeFagOmraader: List<String>,
    ): List<Oppdrag> {
        val statementParts =
            mutableListOf(
                """
                SELECT OS.KODE_STATUS,
                       TRIM(O.OPPDRAGS_ID)         AS OPPDRAGS_ID,
                       TRIM(O.FAGSYSTEM_ID)        AS FAGSYSTEM_ID,
                       TRIM(O.OPPDRAG_GJELDER_ID)  AS OPPDRAG_GJELDER_ID,
                       TRIM(O.KODE_FAGOMRAADE)     AS KODE_FAGOMRAADE,
                       TRIM(FO.NAVN_FAGOMRAADE)    AS NAVN_FAGOMRAADE,
                       TRIM(FO.KODE_FAGGRUPPE)     AS KODE_FAGGRUPPE,
                       FO.ANT_ATTESTANTER          AS ANT_ATTESTANTER,
                       TRIM(FG.NAVN_FAGGRUPPE)     AS NAVN_FAGGRUPPE,
                       TRIM(OK.ENHET)              AS KOSTNADSSTED,
                       TRIM(OA.ENHET)              AS ANSVARSSTED
                FROM T_OPPDRAG O
                         JOIN T_FAGOMRAADE FO ON FO.KODE_FAGOMRAADE = O.KODE_FAGOMRAADE
                         JOIN T_FAGGRUPPE FG ON FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                         JOIN T_OPPDRAGSENHET OK ON OK.OPPDRAGS_ID = O.OPPDRAGS_ID AND OK.TYPE_ENHET = 'BOS'
                         LEFT JOIN T_OPPDRAGSENHET OA ON OA.OPPDRAGS_ID = O.OPPDRAGS_ID AND OA.TYPE_ENHET = 'BEH'
                         JOIN T_OPPDRAG_STATUS OS ON OS.OPPDRAGS_ID = O.OPPDRAGS_ID
                WHERE OS.KODE_STATUS IN ('AKTI', 'PASS')
                  AND NOT EXISTS(SELECT 1 FROM T_OPPDRAG_STATUS OS2 where OS2.OPPDRAGS_ID = OS.OPPDRAGS_ID AND OS2.TIDSPKT_REG > OS.TIDSPKT_REG)
                  AND OK.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                        FROM T_OPPDRAGSENHET OK2
                                        WHERE OK2.OPPDRAGS_ID = OK.OPPDRAGS_ID
                                          AND OK2.TYPE_ENHET = OK.TYPE_ENHET
                                          AND OK2.DATO_FOM <= CURRENT DATE)
                  AND (OA.OPPDRAGS_ID IS NULL
                    OR OA.TIDSPKT_REG = (SELECT MAX(TIDSPKT_REG)
                                         FROM T_OPPDRAGSENHET OA2
                                         WHERE OA2.OPPDRAGS_ID = OA.OPPDRAGS_ID
                                           AND OA2.TYPE_ENHET = OA.TYPE_ENHET
                                           AND OA2.DATO_FOM  <= CURRENT DATE))
                  AND EXISTS( SELECT 1 FROM T_OPPDRAGSLINJE L
                                JOIN T_LINJE_STATUS STATUSNY ON STATUSNY.LINJE_ID = L.LINJE_ID AND STATUSNY.OPPDRAGS_ID = L.OPPDRAGS_ID
                              WHERE STATUSNY.KODE_STATUS = 'NY'
                                AND NOT EXISTS(SELECT KORRANNUOPPH.TIDSPKT_REG
                                               FROM T_LINJE_STATUS KORRANNUOPPH
                                               WHERE KORRANNUOPPH.LINJE_ID = L.LINJE_ID
                                                 AND KORRANNUOPPH.OPPDRAGS_ID = L.OPPDRAGS_ID
                                                 AND KORRANNUOPPH.KODE_STATUS IN ('KORR', 'ANNU', 'OPPH')
                                                 AND KORRANNUOPPH.DATO_FOM = STATUSNY.DATO_FOM
                                                 AND NOT EXISTS(SELECT 1
                                                                FROM T_LINJE_STATUS ANDRESTATUSER
                                                                WHERE ANDRESTATUSER.LINJE_ID = L.LINJE_ID
                                                                  AND ANDRESTATUSER.OPPDRAGS_ID = L.OPPDRAGS_ID
                                                                  AND ANDRESTATUSER.KODE_STATUS IN ('IKAT', 'ATTE', 'HVIL', 'REAK', 'FBER', 'LOPE')
                                                                  AND ANDRESTATUSER.DATO_FOM >= STATUSNY.DATO_FOM
                                                                  AND ANDRESTATUSER.TIDSPKT_REG > KORRANNUOPPH.TIDSPKT_REG))
                                AND L.OPPDRAGS_ID = O.OPPDRAGS_ID
                                ${attestert?.let { " AND L.ATTESTERT = '${if (it) "J" else "N"}'" } ?: ""}
                  )
                """.trimIndent(),
            )

        val parameters = mutableListOf<String>()

        if (!gjelderId.isNullOrBlank()) {
            statementParts.add(" AND O.OPPDRAG_GJELDER_ID = ?")
            parameters.add(gjelderId)
        }

        if (!fagSystemId.isNullOrBlank()) {
            statementParts.add(" AND O.FAGSYSTEM_ID LIKE ?")
            parameters.add("$fagSystemId%")
        }

        if (kodeFagOmraader.isNotEmpty()) {
            statementParts.add(" AND O.KODE_FAGOMRAADE IN (${kodeFagOmraader.joinToString(",") { "?" }})")
            parameters.addAll(kodeFagOmraader)
        }

        statementParts.add(" FETCH FIRST 200 ROWS ONLY")
        if (!gjelderId.isNullOrBlank() || !fagSystemId.isNullOrBlank()) statementParts.add(" OPTIMIZE FOR 1 ROW")

        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    statementParts.joinToString("\n", "", ";"),
                    *parameters.toTypedArray(),
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
            ) { row ->
                FagOmraade(
                    navn = row.string("NAVN_FAGOMRAADE"),
                    kode = row.string("KODE_FAGOMRAADE"),
                )
            }
        }
    }

    fun getFagomraaderForFaggruppe(kodeFaggruppe: String): List<String> {
        val query =
            """
            SELECT TRIM(FO.KODE_FAGOMRAADE) AS KODE_FAGOMRAADE                               
            FROM T_FAGOMRAADE FO 
            JOIN T_FAGGRUPPE FG ON FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
            and fg.KODE_FAGGRUPPE = :KODEFAGGRUPPE
            """.trimIndent()
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    query,
                    mapOf(
                        "KODEFAGGRUPPE" to kodeFaggruppe,
                    ),
                ),
            ) { row -> row.string("KODE_FAGOMRAADE") }
        }
    }

    fun getOppdragslinjer(oppdragsId: Int): List<Oppdragslinje> {
        val query =
            """
            SELECT  L.OPPDRAGS_ID          AS OPPDRAGS_ID,
                    L.LINJE_ID             AS LINJE_ID,
                    TRIM(L.KODE_KLASSE)    AS KODE_KLASSE,
                    L.DATO_VEDTAK_FOM      AS DATO_VEDTAK_FOM,
                    L.DATO_VEDTAK_TOM      AS DATO_VEDTAK_TOM,
                    L.ATTESTERT            AS ATTESTERT,
                    L.SATS                 AS SATS,
                    TRIM(L.TYPE_SATS)      AS TYPE_SATS,
                    TRIM(L.DELYTELSE_ID)   AS DELYTELSE_ID
            FROM T_OPPDRAGSLINJE L
                     JOIN T_LINJE_STATUS STATUSNY ON STATUSNY.LINJE_ID = L.LINJE_ID AND STATUSNY.OPPDRAGS_ID = L.OPPDRAGS_ID
            WHERE STATUSNY.KODE_STATUS = 'NY'
               AND NOT EXISTS(SELECT 1
                             FROM T_LINJE_STATUS KORRANNUOPPH
                             WHERE KORRANNUOPPH.LINJE_ID = L.LINJE_ID
                               AND KORRANNUOPPH.OPPDRAGS_ID = L.OPPDRAGS_ID
                               AND KORRANNUOPPH.KODE_STATUS IN ('KORR', 'ANNU', 'OPPH')
                               AND KORRANNUOPPH.DATO_FOM = STATUSNY.DATO_FOM
                               AND NOT EXISTS(SELECT 1
                                              FROM T_LINJE_STATUS ANDRESTATUSER
                                              WHERE ANDRESTATUSER.LINJE_ID = L.LINJE_ID
                                                AND ANDRESTATUSER.OPPDRAGS_ID = L.OPPDRAGS_ID
                                                AND ANDRESTATUSER.KODE_STATUS IN ('IKAT', 'ATTE', 'HVIL', 'REAK', 'FBER', 'LOPE')
                                                AND ANDRESTATUSER.DATO_FOM >= STATUSNY.DATO_FOM
                                                AND ANDRESTATUSER.TIDSPKT_REG > KORRANNUOPPH.TIDSPKT_REG))
              AND L.OPPDRAGS_ID = :OPPDRAGSID
            ORDER BY KODE_KLASSE, LINJE_ID, DATO_VEDTAK_FOM;
            """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    query,
                    mapOf(
                        "OPPDRAGSID" to oppdragsId,
                    ),
                ),
                mapToOppdragslinje,
            )
        }
    }

    fun getEnhetForLinjer(
        oppdragsId: Int,
        linjeIder: List<Int>,
        typeEnhet: String,
    ): Map<Int, String> {
        if (linjeIder.isEmpty()) return emptyMap()
        val query =
            """
            SELECT LINJE_ID, ENHET
            FROM T_LINJEENHET E
            WHERE 1=1
              AND E.TYPE_ENHET = :TYPEENHET
              AND ENHET != ''
              AND E.OPPDRAGS_ID = :OPPDRAGSID
              AND LINJE_ID IN (${linjeIder.joinToString(",")})              
              AND NOT EXISTS(SELECT 1
                             FROM T_LINJEENHET DUP
                             WHERE E.OPPDRAGS_ID = DUP.OPPDRAGS_ID
                               AND E.LINJE_ID = DUP.LINJE_ID
                               AND E.TIDSPKT_REG < DUP.TIDSPKT_REG);
            """.trimIndent()

        val parameters = mutableListOf(typeEnhet, oppdragsId)
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    query,
                    mapOf(
                        "OPPDRAGSID" to oppdragsId,
                        "TYPEENHET" to typeEnhet,
                    ),
                ),
            ) { row -> row.int("LINJE_ID") to row.string("ENHET") }.toMap()
        }
    }

    fun getAttestasjonerForLinjer(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): Map<Int, List<Attestasjon>> {
        if (linjeIder.isEmpty()) return emptyMap()
        val query =
            """
            SELECT A.LINJE_ID           AS LINJE_ID, 
                   TRIM(A.ATTESTANT_ID) AS ATTESTANT_ID, 
                   A.DATO_UGYLDIG_FOM   AS DATO_UGYLDIG_FOM
            FROM T_ATTESTASJON A
            WHERE 1 = 1
              AND A.LINJE_ID IN (${linjeIder.joinToString(",")})
              AND A.OPPDRAGS_ID = :OPPDRAGSID
              AND (A.LOPENR =
                   (SELECT MAX(A2.LOPENR)
                    FROM T_ATTESTASJON A2
                    WHERE A2.OPPDRAGS_ID = A.OPPDRAGS_ID
                      AND A2.LINJE_ID = A.LINJE_ID
                      AND A2.ATTESTANT_ID = A.ATTESTANT_ID))
              AND A.DATO_UGYLDIG_FOM > CURRENT DATE
            ;
            """.trimIndent()
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    query,
                    mapOf(
                        "OPPDRAGSID" to oppdragsId,
                    ),
                ),
            ) { row ->
                row.int("LINJE_ID") to mapToAttestasjon(row)
            }.groupBy({ it.first }, { it.second })
        }
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarsSted = row.stringOrNull("ANSVARSSTED"),
            antallAttestanter = row.int("ANT_ATTESTANTER"),
            fagSystemId = row.string("FAGSYSTEM_ID"),
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
            kostnadsSted = row.string("KOSTNADSSTED"),
            fagGruppe = row.string("NAVN_FAGGRUPPE"),
            fagOmraade = row.string("NAVN_FAGOMRAADE"),
            oppdragsId = row.int("OPPDRAGS_ID"),
            kodeFagOmraade = row.string("KODE_FAGOMRAADE"),
            kodeFagGruppe = row.string("KODE_FAGGRUPPE"),
        )
    }

    private val mapToOppdragslinje: (Row) -> Oppdragslinje = { row ->
        Oppdragslinje(
            oppdragsId = row.int("OPPDRAGS_ID"),
            linjeId = row.int("LINJE_ID"),
            kodeKlasse = row.string("KODE_KLASSE"),
            datoVedtakFom = row.localDate("DATO_VEDTAK_FOM"),
            datoVedtakTom = row.localDateOrNull("DATO_VEDTAK_TOM"),
            attestert = row.string("ATTESTERT") == "J",
            sats = row.double("SATS"),
            typeSats = row.string("TYPE_SATS"),
            delytelseId = row.string("DELYTELSE_ID"),
        )
    }

    private val mapToAttestasjon: (Row) -> Attestasjon = { row ->
        Attestasjon(
            attestant = row.string("ATTESTANT_ID"),
            datoUgyldigFom = row.localDate("DATO_UGYLDIG_FOM"),
        )
    }
}
