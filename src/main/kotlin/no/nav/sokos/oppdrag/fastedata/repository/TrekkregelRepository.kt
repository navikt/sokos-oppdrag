package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Trekkregel

class TrekkregelRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getTrekkregler(): List<Trekkregel> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        tt.KODE_TREKKTYPE,
                        tt.BESKRIVELSE,
                        tt.PRIORITET,
                        tt.KODE_KLASSE_TREKK,
                        tr.KODE_FAGOMRAADE,
                        tt.ANT_DAGER_OPPF,
                        tt.ANT_DAGER_OPPF_UTF,
                        tt.BELOPSGRENSE,
                        tt.OPPFOLGING,
                        tt.KODE_OPPGJORSTYPE,
                        tt.KODE_OPPGJORSTYPE_NEG
                    FROM T1_TREKKTYPE tt
                    INNER JOIN T1_TREKKREGEL tr
                        ON tr.KODE_TREKKTYPE = tt.KODE_TREKKTYPE
                    ORDER BY tr.KODE_FAGOMRAADE, tt.KODE_TREKKTYPE
                    """.trimIndent(),
                ),
            ) { row ->
                Trekkregel(
                    kodeTrekktype = row.string("KODE_TREKKTYPE").trim(),
                    beskrivelse = row.string("BESKRIVELSE").trim(),
                    prioritet = row.int("PRIORITET"),
                    kodeKlasseTrekk = row.string("KODE_KLASSE_TREKK").trim(),
                    kodeFagomraade = row.string("KODE_FAGOMRAADE").trim(),
                    antDagerOppf = row.intOrNull("ANT_DAGER_OPPF"),
                    antDagerOppfUtf = row.intOrNull("ANT_DAGER_OPPF_UTF"),
                    belopsgrense = row.double("BELOPSGRENSE"),
                    oppfolging = row.string("OPPFOLGING").trim(),
                    kodeOppgjorstype = row.string("KODE_OPPGJORSTYPE").trim(),
                    kodeOppgjorstypeNeg = row.string("KODE_OPPGJORSTYPE_NEG").trim(),
                )
            }
        }
}
