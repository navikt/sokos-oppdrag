package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Trekkgruppe

class TrekkgruppeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getTrekkgrupper(): List<Trekkgruppe> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT 
                        KODE_TREKKGRUPPE,
                        KODE_FAGOMRAADE
                    FROM T1_FAGOMR_GRUPPE
                    ORDER BY KODE_TREKKGRUPPE, KODE_FAGOMRAADE
                    """.trimIndent(),
                ),
            ) { row ->
                Trekkgruppe(
                    kodeTrekkgruppe = row.string("KODE_TREKKGRUPPE").trim(),
                    kodeFagomraade = row.string("KODE_FAGOMRAADE").trim(),
                )
            }
        }
}
