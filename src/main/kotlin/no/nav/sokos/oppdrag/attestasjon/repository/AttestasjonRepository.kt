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
                    select TRIM(g.navn_faggruppe) AS navn_faggruppe
                         , TRIM(f.navn_fagomraade) AS navn_fagomraade
                         , o.oppdrag_gjelder_id
                         , o.oppdrags_id
                         , TRIM(o.fagsystem_id) AS fagsystem_id
                         , ls.kode_status
                    from t_faggruppe g join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                      join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                      join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                      join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                      join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                      left outer join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
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
                    select o.oppdrags_id
                         , l.linje_id
                         , o.oppdrag_gjelder_id
                         , g.navn_faggruppe
                         , f.navn_fagomraade
                         , o.kode_fagomraade
                         , o.fagsystem_id
                         , l.kode_klasse
                         , l.delytelse_id
                         , l.sats
                         , l.type_sats
                         , l.dato_vedtak_fom
                         , l.dato_vedtak_tom
                         , ls.kode_status
                         , a.attestant_id
                         , a.dato_ugyldig_fom
                      from t_faggruppe g join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                      join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                      join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                      join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                      join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                      left join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
                      left join t_attestasjon a on a.oppdrags_id = l.oppdrags_id and a.linje_id = l.linje_id
                    where k.oppdrags_id is null
                      and s.kode_status = 'AKTI'
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
                      and o.oppdrags_id  IN (${oppdragsIder.joinToString()})
                    union
                    select o.oppdrags_id
                        , l.linje_id
                        , o.oppdrag_gjelder_id
                        , g.navn_faggruppe
                        , f.navn_fagomraade
                        , o.kode_fagomraade
                        , o.fagsystem_id
                        , l.kode_klasse
                        , l.delytelse_id
                        , l.sats
                        , l.type_sats
                        , l.dato_vedtak_fom
                        , l2.dato_vedtak_fom - 1 day as dato_vedtak_tom
                        , ls.kode_status
                        , a.attestant_id
                        , a.dato_ugyldig_fom
                     from t_faggruppe g join t_fagomraade f on g.kode_faggruppe = f.kode_faggruppe
                     join t_oppdrag o on f.kode_fagomraade = o.kode_fagomraade
                     join t_oppdragslinje l on o.oppdrags_id = l.oppdrags_id
                     join t_oppdrag_status s on s.oppdrags_id = l.oppdrags_id
                     join t_linje_status ls on ls.oppdrags_id = l.oppdrags_id and ls.linje_id = l.linje_id
                     join t_korreksjon k on l.oppdrags_id = k.oppdrags_id and l.linje_id = k.linje_id
                     join t_oppdragslinje l2 on l2.oppdrags_id = k.oppdrags_id_korr and l2.linje_id = k.linje_id_korr and l.dato_vedtak_fom < l2.dato_vedtak_fom
                     left join t_attestasjon a on a.oppdrags_id = l.oppdrags_id and a.linje_id = l.linje_id
                    where s.kode_status = 'AKTI'
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
            gjelderId = row.string("oppdrag_gjelder_id"),
            navnFagGruppe = row.string("navn_faggruppe"),
            navnFagOmraade = row.string("navn_fagomraade"),
            oppdragsId = row.int("oppdrags_id"),
            fagsystemId = row.string("fagsystem_id"),
        )
    }

    private val mapToOppdragslinjerTilAttestasjon: (Row) -> OppdragsDetaljer = { row ->
        OppdragsDetaljer(
            klasse = row.string("kode_klasse"),
            delytelsesId = row.string("delytelse_id"),
            sats = row.double("sats"),
            satstype = row.string("type_sats"),
            datoVedtakFom = row.string("dato_vedtak_fom"),
            datoVedtakTom = row.stringOrNull("dato_vedtak_tom"),
            attestant = row.string("attestant_id"),
            fagsystemId = row.string("fagsystem_id"),
            navnFagOmraade = row.string("navn_fagomraade"),
        )
    }

    private val mapToFagOmraade: (Row) -> FagOmraade = { row ->
        FagOmraade(
            navn = row.string("NAVN_FAGOMRAADE"),
            kode = row.string("KODE_FAGOMRAADE"),
        )
    }
}
