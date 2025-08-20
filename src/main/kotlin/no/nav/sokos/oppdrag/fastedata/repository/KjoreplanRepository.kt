package no.nav.sokos.oppdrag.fastedata.repository

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val frontendFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private fun fmt(src: String?): String? =
        try {
            src?.let { LocalDate.parse(it, dbFormatter).format(frontendFormatter) }
        } catch (_: Exception) {
            src
        }

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
                val fomRaw = row.string("DATO_BEREGN_FOM")
                val tomRaw = row.string("DATO_BEREGN_TOM")
                val beregningsperiode = listOfNotNull(fmt(fomRaw), fmt(tomRaw)).joinToString("-")

                Kjoreplan(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    datoKjores = fmt(row.string("DATO_KJORES")) ?: "",
                    status = row.string("STATUS"),
                    datoForfall = fmt(row.string("DATO_FORFALL")) ?: "",
                    datoOverfores = fmt(row.stringOrNull("DATO_OVERFORES")),
                    beregningsperiode = beregningsperiode,
                )
            }
        }
}
