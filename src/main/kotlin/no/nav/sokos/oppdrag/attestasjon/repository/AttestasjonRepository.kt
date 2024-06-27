package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.attestasjon.domain.AttestasjonTreff
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjonsdetaljer
import no.nav.sokos.oppdrag.config.DatabaseConfig

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun sok(
        gjelderId: String,
        fagsystemId: String,
        kodeFaggruppe: String,
        kodeFagomraade: String,
        attestert: String,
    ): List<AttestasjonTreff> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    select g.navn_faggruppe
                         , f.navn_fagomraade
                         , o.oppdrags_id
                         , o.fagsystem_id
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
                      and (:gjelderId = ' ' or o.oppdrag_gjelder_id = :gjelderId)
                      and (:fagsystemId = ' ' or o.fagsystem_id = :fagsystemId)
                      and (:kodeFagomraade = ' ' or f.kode_fagomraade = :kodeFagomraade)
                      and (:kodeFaggruppe = ' ' or g.kode_faggruppe = :kodeFaggruppe)
                      and l.attestert like :attestert
                    order by oppdrags_id
                    fetch first 200 rows only
                    """.trimIndent() +
                        (if (gjelderId.isNotBlank()) " optimize for 1 row " else "") +
                        " for fetch only ",
                    mapOf(
                        "gjelderId" to gjelderId,
                        "fagsystemId" to fagsystemId,
                        "kodeFagomraade" to kodeFagomraade,
                        "kodeFaggruppe" to kodeFaggruppe,
                        "attestert" to attestert,
                    ),
                ),
                mapToAttestasjonTreff,
            )
        }
    }

    fun hentOppdragslinjer(oppdragsId: Int): List<Attestasjonsdetaljer> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    select
                        oppdragslinje.kode_klasse     as kode,
                        oppdragslinje.delytelse_id    as delytelse_id,
                        oppdragslinje.sats            as sats,
                        oppdragslinje.type_sats       as type_sats,
                        oppdragslinje.dato_vedtak_fom as dato_vedtak_fom,
                        l2.dato_vedtak_fom - 1 day    as dato_vedtak_tom,
                        oppdragslinje.attestert       as attestert,
                        attestasjon.attestant_id      as attestant_id,
                        oppdrag.fagsystem_id          as fagsystem_id,
                        fagomraade.navn_fagomraade    as navn_fagomraade
                    from t_oppdragslinje oppdragslinje
                             left outer join T_KORREKSJON k on oppdragslinje.OPPDRAGS_ID = k.OPPDRAGS_ID and oppdragslinje.LINJE_ID = k.LINJE_ID
                             left outer join T_OPPDRAGSLINJE l2 on l2.OPPDRAGS_ID = oppdragslinje.OPPDRAGS_ID and l2.LINJE_ID = k.LINJE_ID_KORR
                             join t_oppdrag oppdrag on oppdrag.oppdrags_id = oppdragslinje.oppdrags_id
                             join t_fagomraade fagomraade on fagomraade.kode_fagomraade = oppdrag.kode_fagomraade
                             left outer join t_attestasjon attestasjon
                                             on attestasjon.oppdrags_id = oppdragslinje.oppdrags_id
                                                 and attestasjon.linje_id = oppdragslinje.linje_id
                    where 1=1
                        and (l2.DATO_VEDTAK_FOM is null or l2.DATO_VEDTAK_FOM > oppdragslinje.DATO_VEDTAK_FOM)
                        and oppdragslinje.oppdrags_id = :oppdragsId
                    """.trimIndent(),
                    mapOf("oppdragsId" to oppdragsId),
                ),
                mapToOppdragslinjerTilAttestasjon,
            )
        }
    }

    private val mapToAttestasjonTreff: (Row) -> AttestasjonTreff = { row ->
        AttestasjonTreff(
            navnFaggruppe = row.string("navn_faggruppe").trim(),
            navnFagomraade = row.string("navn_fagomraade").trim(),
            oppdragsId = row.int("oppdrags_id"),
            fagsystemId = row.string("fagsystem_id").trim(),
        )
    }

    private val mapToOppdragslinjerTilAttestasjon: (Row) -> Attestasjonsdetaljer = { row ->
        Attestasjonsdetaljer(
            klasse = row.string("kode").trim(),
            delytelsesId = row.string("delytelse_id").trim(),
            sats = row.double("sats"),
            satstype = row.string("type_sats").trim(),
            datoVedtakFom = row.string("dato_vedtak_fom").trim(),
            datoVedtakTom = row.stringOrNull("dato_vedtak_tom")?.trim(),
            attestert = row.string("attestert").trim(),
            attestant = row.string("attestant_id").trim(),
            fagsystemId = row.string("fagsystem_id").trim(),
            navnFagomraade = row.string("navn_fagomraade").trim(),
        )
    }
}
