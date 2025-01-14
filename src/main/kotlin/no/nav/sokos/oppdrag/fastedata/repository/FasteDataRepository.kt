package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak

class FasteDataRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getFagOmraader(): List<Fagomraade> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE, 
                           TRIM(KODE_FAGOMRAADE) AS KODE_FAGOMRAADE 
                    FROM T_FAGOMRAADE ORDER BY NAVN_FAGOMRAADE
                    """.trimIndent(),
                ),
            ) { row ->
                Fagomraade(
                    navn = row.string("NAVN_FAGOMRAADE"),
                    kode = row.string("KODE_FAGOMRAADE"),
                )
            }
        }

    fun getKorrigeringsaarsakForFagomraade(kodeFagomraade: String): List<Korrigeringsaarsak> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT  TRIM(FK.KODE_FAGOMRAADE) AS FAGOMRAADE,
                            TRIM(K.BESKRIVELSE) AS BESKRIVELSE,
                            TRIM(K.KODE_AARSAK_KORR) AS KODE_KORRIGERINGSAARSAK, 
                            TRIM(FK.MEDFORER_KORR) AS MEDFORER_KORR,    
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
                    navn = row.string("NAVN_KORRIGERINGSAARSAK"),
                    kode = row.string("KODE_KORRIGERINGSAARSAK"),
                    medforerKorrigering = row.string("MEDFORER_KORR") == "J",
                )
            }
        }
}
