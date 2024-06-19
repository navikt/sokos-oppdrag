package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjonsdata
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun sok(gjelderId: String): List<Attestasjonsdata> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    select 
                        g.kode_faggruppe, 
                        g.navn_faggruppe, 
                        o.kode_fagomraade, 
                        f.navn_fagomraade,
                        o.oppdrags_id, 
                        o.fagsystem_id, 
                        o.oppdrag_gjelder_id, 
                        f.ant_attestanter,
                        l.linje_id, 
                        l.attestert, 
                        l.dato_vedtak_fom, 
                        l.dato_vedtak_tom, 
                        ls.kode_status       
                                       
                    from t_faggruppe g join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                        join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                        join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                        join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                        join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                        left outer join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
                    where k.oppdrags_id IS NULL
                    
                    and s.kode_status = 'AKTI'
                    and s.tidspkt_reg = ( select max(s2.tidspkt_reg)
                      from t_oppdrag_status s2
                      where s.oppdrags_id = s2.oppdrags_id)
                    
                    and ls.tidspkt_reg = (select max(tidspkt_reg)
                      from t_linje_status ls2
                      where ls2.oppdrags_id = ls.oppdrags_id
                    and ls2.linje_id = ls.linje_id)
                    
                    and o.oppdrag_gjelder_id 			= :gjelderId
                    
                    order by o.oppdrags_id
                    fetch first 200 rows only
                    for read only
                    optimize for 1 row
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                    ),
                ),
                mapToAttestasjonsdata,
            )
        }
    }

    private val mapToAttestasjonsdata: (Row) -> Attestasjonsdata = { row ->
        Attestasjonsdata(
            kode_faggruppe = row.string("kode_faggruppe").trim(),
            navn_faggruppe = row.string("navn_faggruppe").trim(),
            kode_fagomraade = row.string("kode_fagomraade").trim(),
            navn_fagomraade = row.string("navn_fagomraade").trim(),
            oppdrags_id = row.int("oppdrags_id"),
            fagsystem_id = row.string("fagsystem_id").trim(),
            oppdrag_gjelder_id = row.string("oppdrag_gjelder_id").trim(),
            ant_attestanter = row.int("ant_attestanter"),
            linje_id = row.int("linje_id"),
            attestert = row.string("attestert").trim(),
            dato_vedtak_fom = row.string("dato_vedtak_fom").trim(),
            dato_vedtak_tom = null,
            kode_status = row.string("kode_status").trim(),
        )
    }
}
