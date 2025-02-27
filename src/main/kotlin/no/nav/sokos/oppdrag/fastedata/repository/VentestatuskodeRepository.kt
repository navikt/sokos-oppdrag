package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode

class VentestatuskodeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getAllVentestatuskoder(): List<Ventestatuskode> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT 
                        V.KODE_VENTESTATUS AS KODE_VENTESTATUS,
                        V.BESKRIVELSE AS BESKRIVELSE,
                        V.PRIORITET AS PRIORITET,
                        V.SETTES_MANUELT AS SETTES_MANUELT,
                        V.KODE_ARVES_TIL AS KODE_ARVES_TIL,
                        R.KODE_VENTESTATUS_U AS KAN_MANUELT_ENDRES_TIL
                    FROM T_VENT_STATUSKODE V
                    LEFT JOIN T_VENT_STATUSREGEL R 
                        ON V.KODE_VENTESTATUS = R.KODE_VENTESTATUS_H
                    ORDER BY V.KODE_VENTESTATUS
                    """.trimIndent(),
                ),
            ) { row ->
                Ventestatuskode(
                    kodeVentestatus = row.string("KODE_VENTESTATUS"),
                    beskrivelse = row.string("BESKRIVELSE"),
                    prioritet = row.intOrNull("PRIORITET"),
                    settesManuelt = row.string("SETTES_MANUELT") == "J",
                    kodeArvesTil = row.stringOrNull("KODE_ARVES_TIL"),
                    kanManueltEndresTil = row.stringOrNull("KAN_MANUELT_ENDRES_TIL"),
                )
            }
        }
}
