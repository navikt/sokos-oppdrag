package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Trekkregel
import no.nav.sokos.oppdrag.fastedata.domain.TrekkregelKjoreplan

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
                        CAST(TRIM(tt.PRIORITET) AS INTEGER) AS PRIORITET,
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
                    belopsgrense = row.bigDecimal("BELOPSGRENSE"),
                    oppfolging = row.string("OPPFOLGING").trim(),
                    kodeOppgjorstype = row.string("KODE_OPPGJORSTYPE").trim(),
                    kodeOppgjorstypeNeg = row.string("KODE_OPPGJORSTYPE_NEG").trim(),
                )
            }
        }

    fun getKjoreplan(kodeTrekktype: String): List<TrekkregelKjoreplan> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    SELECT
                        TRIM(k.KODE_OPPGJORSTYPE) AS KODE_OPPGJORSTYPE,
                        k.DATO_KJORES,
                        k.STATUS,
                        k.DATO_PERIODE_FOM,
                        k.DATO_PERIODE_TOM
                    FROM T1_KJOREPLAN_TREKK k
                    INNER JOIN T1_TREKKTYPE t
                        ON t.KODE_OPPGJORSTYPE = k.KODE_OPPGJORSTYPE
                    WHERE TRIM(t.KODE_TREKKTYPE) = :KODE_TREKKTYPE
                    ORDER BY k.DATO_KJORES
                    """.trimIndent(),
                    mapOf("KODE_TREKKTYPE" to kodeTrekktype),
                ),
            ) { row ->
                TrekkregelKjoreplan(
                    kodeOppgjorstype = row.string("KODE_OPPGJORSTYPE"),
                    datoKjores = row.string("DATO_KJORES"),
                    status = row.string("STATUS"),
                    datoPeriodeFom = row.string("DATO_PERIODE_FOM"),
                    datoPeriodeTom = row.string("DATO_PERIODE_TOM"),
                )
            }
        }
}
