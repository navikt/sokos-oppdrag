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
                        v.KODE_VENTESTATUS AS KODE_VENTESTATUS,
                        v.BESKRIVELSE AS BESKRIVELSE,
                        v.TYPE_VENTESTATUS AS TYPE_VENTESTATUS,
                        v.KODE_ARVES_TIL AS KODE_ARVES_TIL,
                        v.SETTES_MANUELT AS SETTES_MANUELT,
                        v.OVERFOR_MOTTKOMP AS OVERFOR_MOTTKOMP,
                        v.PRIORITET AS PRIORITET,
                        (SELECT SUBSTR(xmlserialize(xmlagg(xmltext(', ' || KODE_VENTESTATUS_U)) as CLOB), 3) 
                         FROM T_VENT_STATUSREGEL 
                         WHERE KODE_VENTESTATUS_H = v.KODE_VENTESTATUS) 
                         AS KAN_MANUELT_ENDRES_TIL
                    FROM T_VENT_STATUSKODE v
                    ORDER BY v.KODE_VENTESTATUS
                    """.trimIndent(),
                ),
            ) { row ->
                Ventestatuskode(
                    kodeVentestatus = row.string("KODE_VENTESTATUS"),
                    beskrivelse = row.string("BESKRIVELSE"),
                    prioritet = row.intOrNull("PRIORITET"),
                    settesManuelt = row.boolean("SETTES_MANUELT"),
                    kodeArvesTil = row.stringOrNull("KODE_ARVES_TIL"),
                    kanManueltEndresTil = row.stringOrNull("KAN_MANUELT_ENDRES_TIL"),
                )
            }
        }
}
