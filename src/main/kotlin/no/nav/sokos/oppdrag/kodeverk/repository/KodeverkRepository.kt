package no.nav.sokos.oppdrag.kodeverk.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.kodeverk.domain.FagGruppe
import no.nav.sokos.oppdrag.kodeverk.domain.FagOmraade

class KodeverkRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
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
                    navnFagomraade = row.string("NAVN_FAGOMRAADE"),
                    kodeFagomraade = row.string("KODE_FAGOMRAADE"),
                )
            }
        }

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
