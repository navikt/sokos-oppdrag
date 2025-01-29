package no.nav.sokos.oppdrag.fastedata.repository

import javax.sql.DataSource

import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier

class VentekriterierRepository(private val dataSource: DataSource) {
    fun getVentekriterier(kodeFaggruppe: String): List<Ventekriterier> =
        sessionOf(dataSource).use { session ->
            session.list(
                queryOf(
                    """
                    SELECT KODE_FAGGRUPPE, TYPE_BILAG, DATO_FOM, BELOP_BRUTTO, BELOP_NETTO, 
                           ANT_DAGER_ELDRENN, TIDLIGERE_AAR
                    FROM T_VENT_KRITERIUM
                    WHERE KODE_FAGGRUPPE = :KODE_FAGGRUPPE
                    """.trimIndent(),
                    mapOf("KODE_FAGGRUPPE" to kodeFaggruppe),
                ),
            ) { row ->
                Ventekriterier(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    typeBilag = row.string("TYPE_BILAG"),
                    datoFom = row.string("DATO_FOM"),
                    belopBrutto = row.string("BELOP_BRUTTO"),
                    belopNetto = row.string("BELOP_NETTO"),
                    antDagerEldreEnn = row.intOrNull("ANT_DAGER_ELDRENN"),
                    tidligereAar = row.boolean("TIDLIGERE_AAR"),
                )
            }
        }
}
