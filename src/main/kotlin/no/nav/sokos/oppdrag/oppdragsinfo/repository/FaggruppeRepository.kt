package no.nav.sokos.oppdrag.oppdragsinfo.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe

class FaggruppeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getFagGrupper(): List<FagGruppe> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(NAVN_FAGGRUPPE) AS NAVN_FAGGRUPPE, 
                           TRIM(KODE_FAGGRUPPE) AS KODE_FAGGRUPPE 
                    FROM T_FAGGRUPPE ORDER BY NAVN_FAGGRUPPE
                    """.trimIndent(),
                ),
            ) { row ->
                FagGruppe(
                    navn = row.string("NAVN_FAGGRUPPE").trimIndent(),
                    type = row.string("KODE_FAGGRUPPE").trimIndent(),
                )
            }
        }
}
