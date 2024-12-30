package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.config.DatabaseConfig

class FagomraadeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun getFagOmraader(): List<FagOmraade> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    FROM T_FAGOMRAADE ORDER BY NAVN_FAGOMRAADE
                    """.trimIndent(),
                ),
            ) { row ->
                FagOmraade(
                    navn = row.string("NAVN_FAGOMRAADE"),
                    kode = row.string("KODE_FAGOMRAADE"),
                )
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
}
