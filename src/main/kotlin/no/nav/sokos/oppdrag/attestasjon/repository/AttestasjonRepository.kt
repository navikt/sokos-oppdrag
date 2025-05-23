package no.nav.sokos.oppdrag.attestasjon.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinLocalDate

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.common.util.SqlUtil.sanitizeForSql
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    suspend fun getOppdrag(
        gjelderId: String?,
        fagSystemId: String?,
        kodeFagOmraader: List<String>,
        attestert: Boolean?,
        filterEgenAttestert: Boolean?,
    ): List<Oppdrag> =
        withContext(Dispatchers.IO) {
            buildOppdragSqlQuery(fagSystemId, gjelderId, kodeFagOmraader, attestert).let { (sql, parameterMap) ->
                using(sessionOf(dataSource)) { session ->
                    val oppdragList =
                        session.list(
                            queryOf(
                                sql,
                                parameterMap,
                            ),
                            mapToOppdrag,
                        )
                    when (filterEgenAttestert) {
                        true, false ->
                            oppdragList.map { oppdrag ->
                                oppdrag.apply {
                                    attestanter.putAll(getAttestanterWithOppdragsId(oppdragsId))
                                }
                            }

                        else -> oppdragList
                    }
                }
            }
        }

    fun getOppdragslinjer(oppdragsId: Int): List<Oppdragslinje> {
        val query =
            """
            SELECT  L.OPPDRAGS_ID                AS OPPDRAGS_ID,
                    L.LINJE_ID                   AS LINJE_ID,
                    TRIM(L.KODE_KLASSE)          AS KODE_KLASSE,
                    L.DATO_VEDTAK_FOM            AS DATO_VEDTAK_FOM,
                    L.DATO_VEDTAK_TOM            AS DATO_VEDTAK_TOM,
                    L.ATTESTERT                  AS ATTESTERT,
                    L.SATS                       AS SATS,
                    TRIM(L.TYPE_SATS)            AS TYPE_SATS,
                    TRIM(L.DELYTELSE_ID)         AS DELYTELSE_ID,
                    TRIM(L.KID)                  AS KID,
                    TRIM(L.UTBETALES_TIL_ID)     AS UTBETALES_TIL_ID,
                    TRIM(L.SKYLDNER_ID)          AS SKYLDNER_ID,
                    TRIM(L.REFUNDERES_ID)        AS REFUNDERES_ID,
                    TRIM(kr.hovedkontonr)        AS HOVEDKONTONR,
                    TRIM(kr.underkontonr)        AS UNDERKONTONR,
                    G.GRAD                       as GRAD
            FROM T_OPPDRAGSLINJE L
                     JOIN T_LINJE_STATUS STATUSNY ON STATUSNY.LINJE_ID = L.LINJE_ID AND STATUSNY.OPPDRAGS_ID = L.OPPDRAGS_ID
                     JOIN t_kontoregel kr ON kr.KODE_KLASSE = L.kode_klasse and kr.DATO_FOM <= current_date and kr.DATO_TOM >= current_date
                     LEFT JOIN T_GRAD g on g.linje_id = l.linje_id and g.oppdrags_id = l.oppdrags_id
            WHERE STATUSNY.KODE_STATUS = 'NY'
               and (g.tidspkt_reg is null or g.tidspkt_reg = (select max(tidspkt_reg) from t_grad where linje_id = l.linje_id and oppdrags_id = l.oppdrags_id))              
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

        return using(sessionOf(dataSource)) { session ->
            session
                .list(
                    queryOf(
                        query,
                        mapOf(
                            "OPPDRAGSID" to oppdragsId,
                            "TYPEENHET" to typeEnhet,
                        ),
                    ),
                ) { row -> row.int("LINJE_ID") to row.string("ENHET") }
                .toMap()
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
              ${if (linjeIder.isNotEmpty()) " AND A.LINJE_ID IN (${linjeIder.joinToString(",")})" else ""}
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
            session
                .list(
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

    fun getAttestanterWithOppdragsId(oppdragsId: Int): Map<Int, List<String>> =
        using(sessionOf(dataSource)) { session ->
            session
                .list(
                    queryOf(
                        """
                        SELECT L.LINJE_ID, TRIM(A.ATTESTANT_ID) AS ATTESTANT_ID, A.DATO_UGYLDIG_FOM
                        FROM T_OPPDRAGSLINJE L LEFT OUTER JOIN (
                            SELECT A.ATTESTANT_ID, A.LINJE_ID, A.OPPDRAGS_ID, A.DATO_UGYLDIG_FOM
                            FROM T_ATTESTASJON A
                            WHERE A.LOPENR = (SELECT MAX(A2.LOPENR)
                                              FROM T_ATTESTASJON A2
                                              WHERE A2.OPPDRAGS_ID = A.OPPDRAGS_ID
                                                AND A2.LINJE_ID = A.LINJE_ID
                                                AND A2.ATTESTANT_ID = A.ATTESTANT_ID)
                             AND A.DATO_UGYLDIG_FOM > CURRENT DATE
                        ) A ON (L.OPPDRAGS_ID = A.OPPDRAGS_ID AND L.LINJE_ID = A.LINJE_ID)
                        WHERE L.OPPDRAGS_ID = :OPPDRAGSID
                        """.trimIndent(),
                        mapOf(
                            "OPPDRAGSID" to oppdragsId,
                        ),
                    ),
                ) { row -> row.int("LINJE_ID") to row.stringOrNull("ATTESTANT_ID") }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, attestanter) ->
                    attestanter.filterNotNull().takeIf { it.isNotEmpty() } ?: emptyList()
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

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarssted = row.stringOrNull("ANSVARSSTED"),
            antAttestanter = row.int("ANT_ATTESTANTER"),
            fagSystemId = row.string("FAGSYSTEM_ID"),
            oppdragGjelderId = row.string("OPPDRAG_GJELDER_ID"),
            kostnadssted = row.string("KOSTNADSSTED"),
            navnFaggruppe = row.string("NAVN_FAGGRUPPE"),
            navnFagomraade = row.string("NAVN_FAGOMRAADE"),
            oppdragsId = row.int("OPPDRAGS_ID"),
            kodeFagomraade = row.string("KODE_FAGOMRAADE"),
            kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
        )
    }

    private val mapToOppdragslinje: (Row) -> Oppdragslinje = { row ->
        Oppdragslinje(
            attestert = row.string("ATTESTERT") == "J",
            datoVedtakFom = row.localDate("DATO_VEDTAK_FOM").toKotlinLocalDate(),
            datoVedtakTom = row.localDateOrNull("DATO_VEDTAK_TOM")?.toKotlinLocalDate(),
            delytelseId = row.string("DELYTELSE_ID"),
            kodeKlasse = row.string("KODE_KLASSE"),
            linjeId = row.int("LINJE_ID"),
            oppdragsId = row.int("OPPDRAGS_ID"),
            sats = row.double("SATS"),
            typeSats = row.string("TYPE_SATS"),
            hovedkontonr = row.stringOrNull("HOVEDKONTONR"),
            underkontonr = row.stringOrNull("UNDERKONTONR"),
            grad = row.intOrNull("GRAD"),
            kid = row.stringOrNull("KID"),
            refunderesId = row.stringOrNull("REFUNDERES_ID"),
            skyldnerId = row.stringOrNull("SKYLDNER_ID"),
            utbetalesTilId = row.stringOrNull("UTBETALES_TIL_ID"),
        )
    }

    private val mapToAttestasjon: (Row) -> Attestasjon = { row ->
        Attestasjon(
            attestantId = row.string("ATTESTANT_ID"),
            datoUgyldigFom = row.localDate("DATO_UGYLDIG_FOM").toKotlinLocalDate(),
        )
    }

    private fun buildOppdragSqlQuery(
        fagSystemId: String?,
        gjelderId: String?,
        kodeFagOmraader: List<String>,
        attestert: Boolean?,
    ): Pair<String, Map<String, String>> {
        val parameterMap = mutableMapOf<String, String>()

        val sqlBuilder = StringBuilder()
        sqlBuilder.append(
            """                         
            WITH 
            FilteredOppdrag AS (SELECT OPPDRAGS_ID, KODE_FAGOMRAADE, FAGSYSTEM_ID, OPPDRAG_GJELDER_ID
                                FROM T_OPPDRAG 
            """.trimIndent(),
        )

        var hasWhereClause = false

        if (kodeFagOmraader.isNotEmpty()) {
            sqlBuilder.append(" WHERE KODE_FAGOMRAADE IN (${kodeFagOmraader.joinToString(separator = "','", prefix = "'", postfix = "'") { it.sanitizeForSql() }}) ")
            hasWhereClause = true
        }

        if (!gjelderId.isNullOrBlank()) {
            if (hasWhereClause) {
                sqlBuilder.append(" AND ")
            } else {
                sqlBuilder.append(" WHERE ")
                hasWhereClause = true
            }
            sqlBuilder.append(" OPPDRAG_GJELDER_ID = :GJELDERID")
            parameterMap["GJELDERID"] = gjelderId
        }

        if (!fagSystemId.isNullOrBlank()) {
            if (hasWhereClause) {
                sqlBuilder.append(" AND ")
            } else {
                sqlBuilder.append(" WHERE ")
                hasWhereClause = true
            }
            sqlBuilder.append(" FAGSYSTEM_ID LIKE :FAGSYSTEMID")
            parameterMap["FAGSYSTEMID"] = "$fagSystemId%"
        }
        sqlBuilder.append("),").appendLine()
        sqlBuilder.append(
            """     
            LatestOppdragStatus AS (SELECT OPPDRAGS_ID, MAX(TIDSPKT_REG) AS MaxTIDSPKT_REG
                                    FROM T_OPPDRAG_STATUS
                                    GROUP BY OPPDRAGS_ID),
            ValidLinjeStatus AS (SELECT LINJE_ID, OPPDRAGS_ID
                                 FROM T_LINJE_STATUS
                                 WHERE KODE_STATUS = 'NY'
                                   AND NOT EXISTS (SELECT 1
                                                   FROM T_LINJE_STATUS KORRANNUOPPH
                                                   WHERE KORRANNUOPPH.LINJE_ID = T_LINJE_STATUS.LINJE_ID
                                                     AND KORRANNUOPPH.OPPDRAGS_ID = T_LINJE_STATUS.OPPDRAGS_ID
                                                     AND KORRANNUOPPH.KODE_STATUS IN ('KORR', 'ANNU', 'OPPH')
                                                     AND KORRANNUOPPH.DATO_FOM = T_LINJE_STATUS.DATO_FOM
                                                     AND NOT EXISTS (SELECT 1
                                                                     FROM T_LINJE_STATUS ANDRESTATUSER
                                                                     WHERE ANDRESTATUSER.LINJE_ID = T_LINJE_STATUS.LINJE_ID
                                                                       AND ANDRESTATUSER.OPPDRAGS_ID = T_LINJE_STATUS.OPPDRAGS_ID
                                                                       AND ANDRESTATUSER.KODE_STATUS IN ('IKAT', 'ATTE', 'HVIL', 'REAK', 'FBER', 'LOPE')
                                                                       AND ANDRESTATUSER.DATO_FOM >= T_LINJE_STATUS.DATO_FOM
                                                                       AND ANDRESTATUSER.TIDSPKT_REG > KORRANNUOPPH.TIDSPKT_REG)))
                                                                       
            SELECT DISTINCT OS.KODE_STATUS,
                    TRIM(O.OPPDRAGS_ID)                                AS OPPDRAGS_ID,
                    TRIM(O.FAGSYSTEM_ID)                               AS FAGSYSTEM_ID,
                    TRIM(O.OPPDRAG_GJELDER_ID)                         AS OPPDRAG_GJELDER_ID,
                    TRIM(O.KODE_FAGOMRAADE)                            AS KODE_FAGOMRAADE,
                    TRIM(FO.NAVN_FAGOMRAADE)                           AS NAVN_FAGOMRAADE,
                    TRIM(FO.KODE_FAGGRUPPE)                            AS KODE_FAGGRUPPE,
                    FO.ANT_ATTESTANTER                                 AS ANT_ATTESTANTER,
                    TRIM((SELECT NAVN_FAGGRUPPE
                          FROM T_FAGGRUPPE
                          WHERE KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE))   AS NAVN_FAGGRUPPE,
                    TRIM((SELECT OK.ENHET
                          FROM T_OPPDRAGSENHET OK
                          WHERE OK.OPPDRAGS_ID = O.OPPDRAGS_ID
                            AND OK.TYPE_ENHET = 'BOS'
                            AND OK.TIDSPKT_REG =
                                (SELECT MAX(TIDSPKT_REG)
                                 FROM T_OPPDRAGSENHET OK2
                                 WHERE OK2.OPPDRAGS_ID = OK.OPPDRAGS_ID
                                   AND OK2.TYPE_ENHET = OK.TYPE_ENHET
                                   AND OK2.DATO_FOM <= CURRENT DATE))) AS KOSTNADSSTED,
                    TRIM((SELECT OA.ENHET
                          FROM T_OPPDRAGSENHET OA
                          WHERE OA.OPPDRAGS_ID = O.OPPDRAGS_ID
                            AND OA.TYPE_ENHET = 'BEH'
                            AND OA.TIDSPKT_REG =
                                (SELECT MAX(TIDSPKT_REG)
                                 FROM T_OPPDRAGSENHET OA2
                                 WHERE OA2.OPPDRAGS_ID = OA.OPPDRAGS_ID
                                   AND OA2.TYPE_ENHET = OA.TYPE_ENHET
                                   AND OA2.DATO_FOM <= CURRENT DATE))) AS ANSVARSSTED
                FROM T_OPPDRAGSLINJE L
                JOIN FilteredOppdrag O ON L.OPPDRAGS_ID = O.OPPDRAGS_ID
                JOIN T_FAGOMRAADE FO ON FO.KODE_FAGOMRAADE = O.KODE_FAGOMRAADE
                JOIN ValidLinjeStatus STATUSNY ON STATUSNY.OPPDRAGS_ID = L.OPPDRAGS_ID AND STATUSNY.LINJE_ID = L.LINJE_ID
                JOIN T_OPPDRAG_STATUS OS ON OS.OPPDRAGS_ID = O.OPPDRAGS_ID
                JOIN LatestOppdragStatus LOS ON OS.OPPDRAGS_ID = LOS.OPPDRAGS_ID AND OS.TIDSPKT_REG = LOS.MaxTIDSPKT_REG
                ${attestert?.let { " WHERE L.ATTESTERT = '${if (it) "J" else "N"}'" } ?: ""}    
            """.trimIndent(),
        )
        return Pair(sqlBuilder.toString(), parameterMap)
    }
}
