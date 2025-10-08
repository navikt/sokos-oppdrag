package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Faggruppe
import no.nav.sokos.oppdrag.fastedata.domain.Kjoreplan
import no.nav.sokos.oppdrag.fastedata.domain.RedusertSkatt

class FaggruppeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getFagomraaderForFaggruppe(kodeFaggruppe: String): List<String> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        KODE_FAGOMRAADE AS KODE_FAGOMRAADE
                    FROM T_FAGOMRAADE
                    WHERE KODE_FAGGRUPPE = :KODE_FAGGRUPPE
                    ORDER BY KODE_FAGOMRAADE;
                    """.trimIndent(),
                    mapOf("KODE_FAGGRUPPE" to kodeFaggruppe),
                ),
            ) { row ->
                row.string("KODE_FAGOMRAADE").trim()
            }
        }

    fun getFaggrupper(): List<Faggruppe> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        KODE_FAGGRUPPE       AS KODE_FAGGRUPPE,
                        NAVN_FAGGRUPPE       AS NAVN_FAGGRUPPE,
                        SKATTEPROSENT        AS SKATTEPROSENT,
                        ANT_VENTEDAGER       AS VENTEDAGER,
                        KODE_KLASSE_FEIL     AS KLASSEKODE_FEIL,
                        KODE_KLASSE_JUST     AS KLASSEKODE_JUSTERING,
                        KODE_DESTINASJON     AS DESTINASJON,
                        KODE_RESK_OPPDRAG    AS RESKONTRO_OPPDRAG,
                        KODE_KLASSE_MOTP_FEIL AS KLASSEKODE_MOTP_FEIL,
                        KODE_KLASSE_MOTP_TREKK AS KLASSEKODE_MOTP_TREKK,
                        KODE_KLASSE_MOTP_INNKR AS KLASSEKODE_MOTP_INNKR,
                        PRIORITET_TABELL     AS PRIORITET,
                        ONLINE_BEREGNING     AS ONLINE_BEREGNING,
                        KODE_PENSJON         AS PENSJON,
                        OREAVRUND            AS OEREAVRUNDING,
                        SAMORD_BEREGNING     AS SAMORDNET_BEREGNING,
                        (SELECT COUNT(*) FROM T_FAGOMRAADE fo WHERE fo.KODE_FAGGRUPPE = f.KODE_FAGGRUPPE)
                            AS ANTALL_FAGOMRAADER,
                        (SELECT COUNT(*) FROM T_SKATT_REDUSERT sr WHERE sr.KODE_FAGGRUPPE = f.KODE_FAGGRUPPE)
                            AS ANTALL_REDUSERTSKATT,
                        (SELECT COUNT(*) FROM T_KJOREPLAN kp WHERE kp.KODE_FAGGRUPPE = f.KODE_FAGGRUPPE)
                            AS ANTALL_KJOREPLANER,
                        (SELECT MIN(kplan.DATO_KJORES)
                             FROM T_KJOREPLAN kplan
                             WHERE kplan.KODE_FAGGRUPPE = F.KODE_FAGGRUPPE
                             AND kplan.STATUS='PLAN') 
                             AS NESTE_KJOREDATO
                    FROM T_FAGGRUPPE f;
                    """.trimIndent(),
                ),
            ) { row ->
                Faggruppe(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE").trim(),
                    navnFaggruppe = row.string("NAVN_FAGGRUPPE").trim(),
                    skatteprosent = row.int("SKATTEPROSENT"),
                    ventedager = row.int("VENTEDAGER"),
                    klassekodeFeil = row.string("KLASSEKODE_FEIL").trim(),
                    klassekodeJustering = row.string("KLASSEKODE_JUSTERING").trim(),
                    destinasjon = row.string("DESTINASJON").trim(),
                    reskontroOppdrag = row.string("RESKONTRO_OPPDRAG").trim(),
                    klassekodeMotpFeil = row.string("KLASSEKODE_MOTP_FEIL").trim(),
                    klassekodeMotpTrekk = row.string("KLASSEKODE_MOTP_TREKK").trim(),
                    klassekodeMotpInnkr = row.string("KLASSEKODE_MOTP_INNKR").trim(),
                    prioritet = row.int("PRIORITET"),
                    onlineBeregning = row.string("ONLINE_BEREGNING").let { it == "J" },
                    pensjon = row.string("PENSJON").let { it == "J" },
                    oereavrunding = row.string("OEREAVRUNDING").let { it == "J" },
                    samordnetBeregning = row.string("SAMORDNET_BEREGNING").trim(),
                    antallFagomraader = row.int("ANTALL_FAGOMRAADER"),
                    antallRedusertSkatt = row.int("ANTALL_REDUSERTSKATT"),
                    antallKjoreplaner = row.int("ANTALL_KJOREPLANER"),
                    nesteKjoredato = row.stringOrNull("NESTE_KJOREDATO")?.trim(),
                )
            }
        }

    fun getRedusertSkatt(kodeFaggruppe: String): List<RedusertSkatt> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT 
                        TRIM(KODE_FAGGRUPPE) AS KODE_FAGGRUPPE,
                        DATO_FOM,
                        DATO_TOM,
                        PROSENT_SATS
                    FROM T_SKATT_REDUSERT
                    WHERE TRIM(KODE_FAGGRUPPE) = :KODE_FAGGRUPPE
                    ORDER BY DATO_FOM
                    """.trimIndent(),
                    mapOf("KODE_FAGGRUPPE" to kodeFaggruppe),
                ),
            ) { row ->
                RedusertSkatt(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    datoFom = row.string("DATO_FOM"),
                    datoTom = row.string("DATO_TOM"),
                    prosent = row.int("PROSENT_SATS"),
                )
            }
        }

    fun getKjoreplan(kodeFaggruppe: String): List<Kjoreplan> =
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
                    WHERE TRIM(KODE_FAGGRUPPE) = :KODE_FAGGRUPPE
                    ORDER BY DATO_KJORES
                    """.trimIndent(),
                    mapOf("KODE_FAGGRUPPE" to kodeFaggruppe),
                ),
            ) { row ->
                Kjoreplan(
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    datoKjores = row.string("DATO_KJORES"),
                    status = row.string("STATUS"),
                    datoForfall = row.string("DATO_FORFALL"),
                    datoOverfores = row.stringOrNull("DATO_OVERFORES"),
                    datoBeregnFom = row.string("DATO_BEREGN_FOM"),
                    datoBeregnTom = row.string("DATO_BEREGN_TOM"),
                )
            }
        }
}
