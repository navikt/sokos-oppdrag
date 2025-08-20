
package no.nav.sokos.oppdrag.fastedata.repository

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Faggruppe
import no.nav.sokos.oppdrag.fastedata.domain.RedusertSkatt

class FaggruppeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getFaggrupper(): List<Faggruppe> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        KODE_FAGGRUPPE AS KODE_FAGGRUPPE,
                        NAVN_FAGGRUPPE AS NAVN_FAGGRUPPE,
                        SKATTEPROSENT AS SKATTEPROSENT,
                        ANT_VENTEDAGER AS VENTEDAGER,
                        KODE_KLASSE_FEIL AS KLASSEKODE_FEIL,
                        KODE_KLASSE_JUST AS KLASSEKODE_JUSTERING,
                        KODE_DESTINASJON AS DESTINASJON,
                        KODE_RESK_OPPDRAG AS RESKONTRO_OPPDRAG,
                        KODE_KLASSE_MOTP_FEIL AS KLASSEKODE_MOTP_FEIL,
                        KODE_KLASSE_MOTP_TREKK AS KLASSEKODE_MOTP_TREKK,
                        KODE_KLASSE_MOTP_INNKR AS KLASSEKODE_MOTP_INNKR,
                        PRIORITET_TABELL AS PRIORITET,
                        ONLINE_BEREGNING AS ONLINE_BEREGNING,
                        KODE_PENSJON AS PENSJON,
                        OREAVRUND AS OEREAVRUNDING,
                        SAMORD_BEREGNING AS SAMORDNET_BEREGNING
                    FROM T_FAGGRUPPE;
                    """.trimIndent(),
                ),
            ) { row ->
                Faggruppe(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE").trim(),
                    navnFaggruppe = row.string("NAVN_FAGGRUPPE").trim(),
                    skatteprosent = row.int("SKATTEPROSENT"),
                    ventedager = row.int("VENTEDAGER"),
                    klassekodeFeil = row.stringOrNull("KLASSEKODE_FEIL")?.trim(),
                    klassekodeJustering = row.stringOrNull("KLASSEKODE_JUSTERING")?.trim(),
                    destinasjon = row.stringOrNull("DESTINASJON")?.trim(),
                    reskontroOppdrag = row.stringOrNull("RESKONTRO_OPPDRAG")?.trim(),
                    klassekodeMotpFeil = row.stringOrNull("KLASSEKODE_MOTP_FEIL")?.trim(),
                    klassekodeMotpTrekk = row.stringOrNull("KLASSEKODE_MOTP_TREKK")?.trim(),
                    klassekodeMotpInnkr = row.stringOrNull("KLASSEKODE_MOTP_INNKR")?.trim(),
                    prioritet = row.int("PRIORITET"),
                    onlineBeregning = row.boolean("ONLINE_BEREGNING"),
                    pensjon = row.stringOrNull("PENSJON")?.let { it == "J" },
                    oereavrunding = row.boolean("OEREAVRUNDING"),
                    samordnetBeregning = row.string("SAMORDNET_BEREGNING").trim(),
                )
            }
        }

    fun getRedusertSkatt(kodeFaggruppe: String): List<RedusertSkatt> {
        val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val uiFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT 
                        TRIM(KODE_FAGGRUPPE) AS KODE_FAGGRUPPE,
                        DATO_FOM,
                        DATO_TOM,
                        PROSENT_SATS
                    FROM T_FAGGRUPPE_SKATTETREKK
                    WHERE KODE_FAGGRUPPE = :KODE_FAGGRUPPE
                    ORDER BY DATO_FOM
                    """.trimIndent(),
                    mapOf("KODE_FAGGRUPPE" to kodeFaggruppe),
                ),
            ) { row ->
                val fom = LocalDate.parse(row.string("DATO_FOM"), dbFormatter).format(uiFormatter)
                val tom = LocalDate.parse(row.string("DATO_TOM"), dbFormatter).format(uiFormatter)
                RedusertSkatt(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    periode = "$fom - $tom",
                    prosent = row.int("PROSENT_SATS"),
                )
            }
        }
    }
}
