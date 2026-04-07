package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Trekktype

class TrekktypeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getTrekktyper(): List<Trekktype> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        KODE_TREKKTYPE,
                        BESKRIVELSE,
                        PRIORITET,
                        REDUSER_SKATTEGR,
                        KODE_KLASSE_TREKK,
                        KODE_TREKKATEGORI,
                        TYPE_TREKKBEREGNING
                    FROM T1_TREKKTYPE
                    ORDER BY KODE_TREKKTYPE
                    """.trimIndent(),
                ),
            ) { row ->
                Trekktype(
                    kodeTrekktype = row.string("KODE_TREKKTYPE").trim(),
                    beskrivelse = row.string("BESKRIVELSE").trim(),
                    prioritet = row.string("PRIORITET").trim(),
                    reduserSkattegrl = row.stringOrNull("REDUSER_SKATTEGR")?.trim(),
                    kodeKlasseTrekk = row.stringOrNull("KODE_KLASSE_TREKK")?.trim(),
                    kodeTrekkategori = row.stringOrNull("KODE_TREKKATEGORI")?.trim(),
                    typeTrekkberegning = row.stringOrNull("TYPE_TREKKBEREGNING")?.trim(),
                )
            }
        }
}
