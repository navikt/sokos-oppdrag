package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Kjoreplan

class KjoreplanRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getKjoreplan(): List<Kjoreplan> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT 
                        TRIM(KODE_FAGGRUPPE) AS KODE_FAGGRUPPE,
                        DATO_KJORES,
                        STATUS,
                        DATO_FORFALL,
                        DATO_OVERFORES,
                        DATO_BEREGN_FOM,
                        DATO_BEREGN_TOM
                    FROM T_KJOREPLAN
                    ORDER BY KODE_FAGGRUPPE, DATO_KJORES
                    """.trimIndent(),
                ),
            ) { row ->
                val fom = row.stringOrNull("DATO_BEREGN_FOM")
                val tom = row.stringOrNull("DATO_BEREGN_TOM")
                val beregningsperiode = listOfNotNull(fom, tom).joinToString("-")

                Kjoreplan(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    datoKjores = row.string("DATO_KJORES"),
                    status = row.string("STATUS"),
                    datoForfall = row.string("DATO_FORFALL"),
                    datoOverfores = row.stringOrNull("DATO_OVERFORES"),
                    beregningsperiode = beregningsperiode,
                )
            }
        }
}
