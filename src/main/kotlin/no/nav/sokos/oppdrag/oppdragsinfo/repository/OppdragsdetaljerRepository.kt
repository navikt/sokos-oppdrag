package no.nav.sokos.oppdrag.oppdragsinfo.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta

class OppdragsdetaljerRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getGrader(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Grad> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(TYPE_GRAD) AS TYPE_GRAD, GRAD, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_GRAD
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Grad(
                    linjeId = row.int("LINJE_ID"),
                    typeGrad = row.string("TYPE_GRAD"),
                    grad = row.int("GRAD"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getTekster(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Tekst> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT  LINJE_ID, TEKST
                    FROM T_TEKST
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Tekst(
                    linjeId = row.int("LINJE_ID"),
                    tekst = row.string("TEKST"),
                )
            }
        }

    fun getKid(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Kid> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(KID) AS KID, DATO_FOM, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_KID
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Kid(
                    linjeId = row.int("LINJE_ID"),
                    kid = row.string("KID"),
                    datoFom = row.string("DATO_FOM"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getMaksDatoer(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Maksdato> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, MAKS_DATO, DATO_FOM, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_MAKS_DATO
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Maksdato(
                    linjeId = row.int("LINJE_ID"),
                    maksdato = row.string("MAKS_DATO"),
                    datoFom = row.string("DATO_FOM"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getOvriger(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Ovrig> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(VEDTAK_ID) AS VEDTAK_ID, TRIM(HENVISNING) AS HENVISNING, TRIM(TYPE_SOKNAD) AS TYPE_SOKNAD
                    FROM T_OPPDRAGSLINJE
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Ovrig(
                    linjeId = row.int("LINJE_ID"),
                    vedtaksId = row.string("VEDTAK_ID"),
                    henvisning = row.string("HENVISNING"),
                    typeSoknad = row.string("TYPE_SOKNAD"),
                )
            }
        }

    fun getValutaer(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Valuta> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(TYPE_VALUTA) AS TYPE_VALUTA, DATO_FOM, NOKKEL_ID, VALUTA, FEILREG, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_VALUTA 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Valuta(
                    linjeId = row.int("LINJE_ID"),
                    typeValuta = row.string("TYPE_VALUTA"),
                    datoFom = row.string("DATO_FOM"),
                    nokkelId = row.int("NOKKEL_ID"),
                    valuta = row.string("VALUTA"),
                    feilreg = row.stringOrNull("FEILREG"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getSkyldnere(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Skyldner> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(SKYLDNER_ID) AS SKYLDNER_ID, DATO_FOM, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_SKYLDNER 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Skyldner(
                    linjeId = row.int("LINJE_ID"),
                    skyldnerId = row.string("SKYLDNER_ID"),
                    datoFom = row.string("DATO_FOM"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getKravhavere(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Kravhaver> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(KRAVHAVER_ID) AS KRAVHAVER_ID, DATO_FOM, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_KRAVHAVER 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                Kravhaver(
                    linjeId = row.int("LINJE_ID"),
                    kravhaverId = row.string("KRAVHAVER_ID"),
                    datoFom = row.string("DATO_FOM"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }

    fun getEnheter(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<LinjeEnhet> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TRIM(TYPE_ENHET) AS TYPE_ENHET, TRIM(ENHET) ENHET, DATO_FOM, NOKKEL_ID, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
                    FROM T_LINJEENHET
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row ->
                LinjeEnhet(
                    linjeId = row.int("LINJE_ID"),
                    typeEnhet = row.string("TYPE_ENHET"),
                    enhet = row.string("ENHET"),
                    datoFom = row.string("DATO_FOM"),
                    nokkelId = row.int("NOKKEL_ID"),
                    tidspktReg = row.string("TIDSPKT_REG"),
                    brukerid = row.string("BRUKERID"),
                )
            }
        }
}
