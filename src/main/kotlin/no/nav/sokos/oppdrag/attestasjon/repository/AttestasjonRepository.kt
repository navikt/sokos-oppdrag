package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun getOppdrag(
        gjelderId: String,
        fagsystemId: String,
        kodeFaggruppe: String,
        kodeFagomraade: String,
        attestert: Boolean?,
    ): List<Oppdrag> {
        return using(sessionOf(dataSource)) { session ->
            val statementParts =
                mutableListOf(
                    """
                    select TRIM(g.navn_faggruppe)                             AS navn_faggruppe
                         , TRIM(f.navn_fagomraade)                            AS navn_fagomraade
                         , o.oppdrag_gjelder_id                               AS oppdrag_gjelder_id
                         , o.oppdrags_id                                      AS oppdrags_id
                         , TRIM(o.fagsystem_id)                               AS fagsystem_id
                         , ls.kode_status                                     AS kode_status
                         , oe.ENHET                                           AS kostnadssted
                         , case when le.enhet is not null then TRIM(le.enhet)
                          when oeb.enhet is not null then TRIM(oeb.enhet)
                          else TRIM(oe.enhet) end                             AS ansvarssted
                    from t_faggruppe g
                             join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                             join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                             join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                             join t_oppdragsenhet oe on oe.oppdrags_id = o.oppdrags_id and oe.type_enhet = 'BOS'
                             join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                             join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                             left outer join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
                             left outer join t_linjeenhet le on le.oppdrags_id = l.oppdrags_id and le.type_enhet = 'BEH' and le.linje_id = l.linje_id
                             left outer join t_oppdragsenhet oeb on oeb.oppdrags_id = o.oppdrags_id and oeb.type_enhet = 'BEH'
                    where k.oppdrags_id IS NULL
                      and s.kode_status = 'AKTI'
                      and s.tidspkt_reg = (select max(s2.tidspkt_reg)
                                           from t_oppdrag_status s2
                                           where s.oppdrags_id = s2.oppdrags_id)
                      and ls.tidspkt_reg = (select max(tidspkt_reg)
                                            from t_linje_status ls2
                                            where ls2.oppdrags_id = ls.oppdrags_id
                                              and ls2.linje_id = ls.linje_id)
                    """.trimIndent(),
                )

            if (gjelderId.isNotBlank()) statementParts.add("and o.oppdrag_gjelder_id = :gjelderId")
            if (fagsystemId.isNotBlank()) statementParts.add("and o.fagsystem_id = :fagsystemId")
            if (kodeFagomraade.isNotBlank()) statementParts.add("and f.kode_fagomraade = :kodeFagomraade")
            if (kodeFaggruppe.isNotBlank()) statementParts.add("and g.kode_faggruppe = :kodeFaggruppe")

            if (attestert == false) {
                statementParts.add("and l.attestert = 'N'")
            } else if (attestert == true) {
                statementParts.add("and l.attestert = 'J'")
            } else if (attestert == null) {
                statementParts.add("and l.attestert like '%'")
            }

            statementParts.add("fetch first 200 rows only")
            statementParts.add("group by navn_faggruppe, navn_fagomraade, oppdrag_gjelder_id, o.oppdrags_id, fagsystem_id, ls.kode_status, oe.enhet, oeb.enhet")
            if (gjelderId.isNotBlank() || fagsystemId.isNotBlank()) statementParts.add("optimize for 1 row")

            session.list(
                queryOf(
                    statementParts.joinToString("\n"),
                    mapOf(
                        "gjelderId" to gjelderId,
                        "fagsystemId" to fagsystemId,
                        "kodeFagomraade" to kodeFagomraade,
                        "kodeFaggruppe" to kodeFaggruppe,
                        "attestert" to attestert,
                    ),
                ),
                mapToOppdrag,
            )
        }
    }

    fun getFagOmraader(): List<FagOmraade> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    select TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    from T_FAGOMRAADE
                    """.trimIndent(),
                ),
                mapToFagOmraade,
            )
        }
    }

    fun getOppdragsDetaljer(oppdragsIder: List<Int>): List<OppdragsDetaljer> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                         select o.oppdrags_id                                       as oppdrags_id
                         , f.ANT_ATTESTANTER                                        as ANT_ATTESTANTER
                         , l.linje_id                                               as linje_id
                         , o.oppdrag_gjelder_id                                     as oppdrag_gjelder_id
                         , TRIM(g.navn_faggruppe)                                   AS navn_faggruppe
                         , TRIM(f.navn_fagomraade)                                  AS navn_fagomraade
                         , o.kode_fagomraade                                        as kode_fagomraade
                         , TRIM(o.fagsystem_id)                                     AS fagsystem_id
                         , TRIM(l.kode_klasse)                                      AS kode_klasse
                         , TRIM(l.delytelse_id)                                     AS delytelse_id
                         , l.sats                                                   as sats
                         , TRIM(l.type_sats)                                        AS type_sats
                         , l.dato_vedtak_fom                                        as dato_vedtak_fom
                         , coalesce(l2.dato_vedtak_fom - 1 day, l.dato_vedtak_tom)  as dato_vedtak_tom
                         , TRIM(a.attestant_id)                                     AS attestant_id
                         , a.dato_ugyldig_fom                                       as dato_ugyldig_fom
                         , TRIM(oe.enhet)                                           AS kostnadssted
                         , case when le.enhet is not null then TRIM(le.enhet)
                                when oeb.enhet is not null then TRIM(oeb.enhet)
                                else TRIM(oe.enhet)
                        end                                                         as ansvarssted
                    from t_faggruppe g
                             join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                             join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                             join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                             join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                             join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                             join t_oppdragsenhet oe on oe.oppdrags_id = o.oppdrags_id and oe.type_enhet = 'BOS'
                             join T_STATUSKODE sk on sk.KODE_STATUS = ls.KODE_STATUS
                             left join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
                             left join t_oppdrag o2 on k.oppdrags_id_korr = o2.oppdrags_id and f.kode_fagomraade = o2.kode_fagomraade
                             left join t_oppdragslinje l2 on l2.oppdrags_id = o2.oppdrags_id and k.linje_id_korr = l2.linje_id and l.dato_vedtak_fom < l2.dato_vedtak_fom
                             left join t_attestasjon a on a.oppdrags_id = l.oppdrags_id and a.linje_id = l.linje_id and a.dato_ugyldig_fom > current date
                             left outer join t_linjeenhet le on le.oppdrags_id = l.oppdrags_id and le.type_enhet = 'BEH' and le.linje_id = l.linje_id
                             left outer join t_oppdragsenhet oeb on oeb.oppdrags_id = o.oppdrags_id and oeb.type_enhet = 'BEH'
                    where s.kode_status = 'AKTI'
                      and not (l2.DATO_VEDTAK_FOM is null and sk.TYPE_STATUS != 'AKTI')
                      and s.tidspkt_reg = (select max(s2.tidspkt_reg)
                                           from t_oppdrag_status s2
                                           where s.oppdrags_id = s2.oppdrags_id)
                      and ls.tidspkt_reg = (select max(tidspkt_reg)
                                            from t_linje_status ls2
                                            where ls2.oppdrags_id = ls.oppdrags_id
                                              and ls2.linje_id = ls.linje_id)
                      and (a.oppdrags_id is null or a.lopenr = (select max(a2.lopenr)
                                                                from t_attestasjon a2
                                                                where a2.oppdrags_id = l.oppdrags_id
                                                                  and a2.linje_id = l.linje_id
                                                                  and a2.attestant_id = a.attestant_id))
                      and oe.tidspkt_reg = (select max(tidspkt_reg)
                                            from t_oppdragsenhet oe2
                                            where oe2.oppdrags_id = oe.oppdrags_id
                                              and oe2.type_enhet  = oe.type_enhet
                                              and oe2.dato_fom   <= current date)
                      and (le.tidspkt_reg is null or le.tidspkt_reg = (select max(tidspkt_reg)
                                                                       from t_linjeenhet le2
                                                                       where le2.oppdrags_id = le.oppdrags_id
                                                                         and le2.linje_id    = le.linje_id
                                                                         and le2.type_enhet  = le.type_enhet
                                                                         and le2.dato_fom   <= current date))
                      and (oeb.tidspkt_reg is null or oeb.tidspkt_reg = (select max(tidspkt_reg)
                                                                         from t_oppdragsenhet oeb2
                                                                         where oeb2.oppdrags_id = oeb.oppdrags_id
                                                                           and oeb2.type_enhet  = oeb.type_enhet
                                                                           and oeb2.dato_fom   <= current date))
                      and o.oppdrags_id  IN (${oppdragsIder.joinToString()})
                    order by oppdrags_id, LINJE_ID
                    """.trimIndent(),
                ),
                mapToOppdragslinjerTilAttestasjon,
            )
        }
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            ansvarsSted = row.string("ansvarssted"),
            fagsystemId = row.string("fagsystem_id"),
            gjelderId = row.string("oppdrag_gjelder_id"),
            kostnadsSted = row.string("kostnadssted"),
            navnFagGruppe = row.string("navn_faggruppe"),
            navnFagOmraade = row.string("navn_fagomraade"),
            oppdragsId = row.int("oppdrags_id"),
        )
    }

    private val mapToOppdragslinjerTilAttestasjon: (Row) -> OppdragsDetaljer = { row ->
        OppdragsDetaljer(
            ansvarsSted = row.string("ansvarssted"),
            antallAttestanter = row.int("ANT_ATTESTANTER"),
            attestant = row.stringOrNull("attestant_id"),
            datoUgyldigFom = row.stringOrNull("dato_ugyldig_fom"),
            datoVedtakFom = row.string("dato_vedtak_fom"),
            datoVedtakTom = row.stringOrNull("dato_vedtak_tom"),
            delytelsesId = row.string("delytelse_id"),
            navnFagGruppe = row.string("navn_faggruppe"),
            navnFagOmraade = row.string("navn_fagomraade"),
            fagSystemId = row.string("fagsystem_id"),
            klasse = row.string("kode_klasse"),
            kostnadsSted = row.string("kostnadssted"),
            linjeId = row.string("linje_id"),
            oppdragGjelderId = row.string("oppdrag_gjelder_id"),
            sats = row.double("sats"),
            satstype = row.string("type_sats"),
        )
    }

    private val mapToFagOmraade: (Row) -> FagOmraade = { row ->
        FagOmraade(
            navn = row.string("NAVN_FAGOMRAADE"),
            kode = row.string("KODE_FAGOMRAADE"),
        )
    }
}
