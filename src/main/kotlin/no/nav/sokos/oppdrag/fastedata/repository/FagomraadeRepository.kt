package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak

class FagomraadeRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getFagOmraader(): List<Fagomraade> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(F.KODE_FAGOMRAADE)                     AS KODE_FAGOMRAADE,
                           TRIM(F.NAVN_FAGOMRAADE)                     AS NAVN_FAGOMRAADE,
                           TRIM(F.KODE_MOTREGNGRUPPE)                  AS KODE_MOTREGNGRUPPE,
                           (SELECT DISTINCT 1 FROM T_FAGOMR_KORRARSAK
                            WHERE KODE_FAGOMRAADE = F.KODE_FAGOMRAADE) AS KORRAARSAK_FINNES,
                           (SELECT DISTINCT 1 FROM T_FAGO_BILAGSTYPE
                            WHERE KODE_FAGOMRAADE = F.KODE_FAGOMRAADE) AS BILAGSTYPE_FINNES,
                           (SELECT DISTINCT 1 FROM T_FAGO_KLASSEKODE
                            WHERE KODE_FAGOMRAADE = F.KODE_FAGOMRAADE) AS KLASSEKODE_FINNES,
                           (SELECT DISTINCT 1 FROM T_FAGOMR_REGEL
                            WHERE KODE_FAGOMRAADE = F.KODE_FAGOMRAADE) AS REGEL_FINNES,
                             TRIM(F.KODE_FAGGRUPPE)                    AS KODE_FAGGRUPPE,
                             TRIM(F.ANT_ATTESTANTER)                   AS ANT_ATTESTANTER,
                             TRIM(F.MAKS_AKT_OPPDRAG)                  AS MAKS_AKT_OPPDRAG,
                             TRIM(F.TPS_DISTRIBUSJON)                  AS TPS_DISTRIBUSJON,
                             TRIM(F.SJEKK_OFFID)                       AS SJEKK_OFFID,
                             TRIM(F.ANVISER)                           AS ANVISER,
                             TRIM(F.SJEKK_MOT_TPS)                     AS SJEKK_MOT_TPS,
                             TRIM(F.BRUKERID)                          AS BRUKERID,
                             F.TIDSPKT_REG                             AS TIDSPKT_REG
                    FROM T_FAGOMRAADE F
                    ORDER BY KODE_FAGOMRAADE ;
                    """.trimIndent(),
                ),
            ) { row ->
                Fagomraade(
                    antallAttestanter = row.int("ANT_ATTESTANTER"),
                    anviser = row.string("ANVISER"),
                    bilagstypeFinnes = row.byteOrNull("BILAGSTYPE_FINNES") != null,
                    klassekodeFinnes = row.byteOrNull("KLASSEKODE_FINNES") != null,
                    kode = row.string("KODE_FAGOMRAADE"),
                    kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
                    kodeMotregningsgruppe = row.string("KODE_MOTREGNGRUPPE"),
                    korraarsakFinnes = row.byteOrNull("KORRAARSAK_FINNES") != null,
                    maksAktiveOppdrag = row.int("MAKS_AKT_OPPDRAG"),
                    navn = row.string("NAVN_FAGOMRAADE"),
                    regelFinnes = row.byteOrNull("REGEL_FINNES") != null,
                    sjekkMotTps = row.string("SJEKK_MOT_TPS"),
                    sjekkOffnrID = row.string("SJEKK_OFFID"),
                    tpsDistribusjon = row.string("TPS_DISTRIBUSJON"),
                )
            }
        }

    fun getKorrigeringsaarsaker(kodeFagomraade: String): List<Korrigeringsaarsak> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT  TRIM(K.BESKRIVELSE)      AS NAVN,
                            TRIM(K.KODE_AARSAK_KORR) AS KODE, 
                            TRIM(FK.MEDFORER_KORR)   AS MEDFORER_KORR    
                    FROM T_FAGOMR_KORRARSAK FK
                    JOIN T_KORR_AARSAK K ON K.KODE_AARSAK_KORR = FK.KODE_AARSAK_KORR 
                    WHERE FK.KODE_FAGOMRAADE = :KODE_FAGOMRAADE   
                    """.trimIndent(),
                    mapOf(
                        "KODE_FAGOMRAADE" to kodeFagomraade,
                    ),
                ),
            ) { row ->
                Korrigeringsaarsak(
                    navn = row.string("NAVN"),
                    kode = row.string("KODE"),
                    medforerKorrigering = row.string("MEDFORER_KORR") == "J",
                )
            }
        }

    fun getBilagstyper(kodeFagomraade: String): List<Bilagstype> =
        using(sessionOf(dataSource)) { session: Session ->
            session.list(
                queryOf(
                    """
                    select KODE_FAGOMRAADE,
                           TYPE_BILAG,
                           DATO_FOM,
                           DATO_TOM,
                           AUTO_FAGSYSTEMID
                     from T_FAGO_BILAGSTYPE
                           WHERE FK.KODE_FAGOMRAADE = :KODE_FAGOMRAADE   
                    """.trimIndent(),
                    mapOf(
                        "KODE_FAGOMRAADE" to kodeFagomraade,
                    ),
                ),
            ) { row ->
                Bilagstype(
                    kode = row.string("KODE_FAGOMRAADE"),
                    type = row.string("TYPE_BILAG"),
                    datoFom = row.string("DATO_FOM"),
                    datoTom = row.string("DATO_TOM"),
                    autoFagsystem = row.string("AUTO_FAGSYSTEMID"),
                )
            }
        }
}
