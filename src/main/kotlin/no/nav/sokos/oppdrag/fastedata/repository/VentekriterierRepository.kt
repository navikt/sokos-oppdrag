package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier

class VentekriterierRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getAllVentekriterier(): List<Ventekriterier> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(V.KODE_FAGGRUPPE) AS KODE_FAGGRUPPE,
                           TRIM(V.TYPE_BILAG) AS TYPE_BILAG,
                           V.DATO_FOM AS DATO_FOM,
                           V.BELOP_BRUTTO AS BELOP_BRUTTO,
                           V.BELOP_NETTO AS BELOP_NETTO,
                           V.ANT_DAGER_ELDREENN AS ANT_DAGER_ELDREENN,
                           V.TIDLIGERE_AAR AS TIDLIGERE_AAR
                    FROM T_VENT_KRITERIUM V
                    ORDER BY V.KODE_FAGGRUPPE
                    """.trimIndent(),
                ),
            ) { row ->
                Ventekriterier(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    typeBilag = row.string("TYPE_BILAG"),
                    datoFom = row.string("DATO_FOM"),
                    belopBrutto = row.doubleOrNull("BELOP_BRUTTO"),
                    belopNetto = row.doubleOrNull("BELOP_NETTO"),
                    antDagerEldreenn = row.intOrNull("ANT_DAGER_ELDREENN"),
                    tidligereAar = row.boolean("TIDLIGERE_AAR"),
                )
            }
        }
}
