package no.nav.sokos.oppdrag.oppdragsinfo.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Korreksjon
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta

class OppdragsInfoRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun hentOppdragsInfo(gjelderId: String): OppdragsInfo? {
        return using(sessionOf(dataSource)) { session ->
            session.single(
                queryOf(
                    """
                    SELECT OPPDRAG_GJELDER_ID FROM T_OPPDRAG WHERE OPPDRAG_GJELDER_ID = :gjelderId
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                    ),
                ),
                mapToOppdragsInfo,
            )
        }
    }

    fun hentFagGrupper(): List<FagGruppe> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT NAVN_FAGGRUPPE, KODE_FAGGRUPPE FROM T_FAGGRUPPE ORDER BY NAVN_FAGGRUPPE
                    """.trimIndent(),
                ),
                mapToFagGruppe,
            )
        }
    }

    fun hentOppdragsListe(
        gjelderId: String,
        fagGruppeKode: String?,
    ): List<Oppdrag> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OP.OPPDRAGS_ID,
                        OP.FAGSYSTEM_ID,
                        FO.NAVN_FAGOMRAADE,
                        OP.OPPDRAG_GJELDER_ID,
                        OP.KJOR_IDAG,
                        OP.TYPE_BILAG,
                        FG.NAVN_FAGGRUPPE,
                        OS.KODE_STATUS,
                        OS.TIDSPKT_REG
                    FROM T_OPPDRAG OP,
                        T_FAGOMRAADE FO, 
                        T_FAGGRUPPE FG,
                        T_OPPDRAG_STATUS OS
                    WHERE OP.OPPDRAG_GJELDER_ID = :gjelderId
                        ${if (fagGruppeKode != null) " AND FG.KODE_FAGGRUPPE = :fagGruppeKode" else ""}
                    AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                    AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                    AND OS.OPPDRAGS_ID = OP.OPPDRAGS_ID
                    AND OS.TIDSPKT_REG = (
                    SELECT MAX(OS2.TIDSPKT_REG)
                    FROM T_OPPDRAG_STATUS OS2
                    WHERE OS2.OPPDRAGS_ID = OS.OPPDRAGS_ID)
                    ORDER BY OS.KODE_STATUS
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                        "fagGruppeKode" to fagGruppeKode,
                    ),
                ),
                mapToOppdrag,
            )
        }
    }

    fun erOppdragTilknyttetBruker(
        gjelderId: String,
        oppdragsId: Int,
    ): Boolean {
        return using(sessionOf(dataSource)) { session ->
            session.single(
                queryOf(
                    """
                    SELECT COUNT(*)
                    FROM T_OPPDRAG
                    WHERE OPPDRAG_GJELDER_ID = :gjelderId
                    AND OPPDRAGS_ID = :oppdragsId
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row -> row.boolean(1) } ?: false
        }
    }

    fun hentOppdragsEnhet(
        typeEnhet: String? = null,
        oppdragsId: Int,
    ): List<OppdragsEnhet> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TYPE_ENHET, DATO_FOM, ENHET 
                    FROM T_OPPDRAGSENHET 
                    WHERE OPPDRAGS_ID = :oppdragsId
                        ${if (typeEnhet != null) " AND TYPE_ENHET = :typeEnhet" else " AND TYPE_ENHET IN (SELECT TYPE_ENHET FROM T_ENHETSTYPE WHERE TYPE_ENHET != 'BEH')"}
                    AND DATO_FOM = (SELECT MAX(DATO_FOM) 
                    FROM T_OPPDRAGSENHET
                    WHERE OPPDRAGS_ID = :oppdragsId
                         ${if (typeEnhet != null) " AND TYPE_ENHET = :typeEnhet)" else " AND TYPE_ENHET IN (SELECT TYPE_ENHET FROM T_ENHETSTYPE WHERE TYPE_ENHET != 'BEH'))"}
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                        "typeEnhet" to typeEnhet,
                    ),
                ),
                mapToOppdragsEnhet,
            )
        }
    }

    fun eksistererOmposteringer(
        gjelderId: String,
        oppdragsId: Int,
    ): Boolean {
        return using(sessionOf(dataSource)) { session ->
            session.single(
                queryOf(
                    """
                    SELECT COUNT(*)
                    FROM T_OMPOSTERING OM,
                        T_OPPDRAG OP,
                        T_FAGOMRAADE FO, 
                        T_FAGGRUPPE FG
                    WHERE OM.GJELDER_ID = :gjelderId
                    AND OP.OPPDRAGS_ID = :oppdragsId
                    AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                    AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                    AND FG.KODE_FAGGRUPPE = OM.KODE_FAGGRUPPE
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                        "oppdragsId" to oppdragsId,
                    ),
                ),
            ) { row -> row.int(1) > 0 } ?: false
        }
    }

    fun hentOppdragsLinjer(oppdragsId: Int): List<OppdragsLinje> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OPLI.LINJE_ID,
                        OPLI.KODE_KLASSE,
                        OPLI.DATO_VEDTAK_FOM,
                        OPLI.DATO_VEDTAK_TOM,
                        OPLI.SATS,
                        OPLI.TYPE_SATS,
                        LIST.KODE_STATUS,
                        LIST.DATO_FOM,
                        OPLI.ATTESTERT,
                        KORR.LINJE_ID_KORR,
                        OPLI.DELYTELSE_ID,
                        OPLI.UTBETALES_TIL_ID,
                        OPLI.REFUNDERES_ID,
                        OPLI.BRUKERID,
                        OPLI.TIDSPKT_REG
                    FROM T_KJOREDATO KJDA, T_OPPDRAGSLINJE OPLI, T_LINJE_STATUS LIST
                    LEFT OUTER JOIN T_KORREKSJON KORR
                        ON LIST.OPPDRAGS_ID = KORR.OPPDRAGS_ID
                        AND LIST.LINJE_ID = KORR.LINJE_ID
                        WHERE OPLI.OPPDRAGS_ID = :oppdragsId
                        AND LIST.OPPDRAGS_ID = OPLI.OPPDRAGS_ID
                        AND LIST.LINJE_ID = OPLI.LINJE_ID
                        AND LIST.TIDSPKT_REG = (SELECT MAX(LIS1.TIDSPKT_REG)
                                            FROM T_LINJE_STATUS LIS1
                                            WHERE LIS1.OPPDRAGS_ID = LIST.OPPDRAGS_ID
                                            AND LIS1.LINJE_ID = LIST.LINJE_ID
                                            AND (CASE WHEN KJDA.KJOREDATO <= LIS1.DATO_FOM
                                            THEN (SELECT MIN(LIS2.DATO_FOM)
                                                  FROM T_LINJE_STATUS LIS2
                                                  WHERE  LIS2.OPPDRAGS_ID = LIST.OPPDRAGS_ID
                                                  AND LIS2.LINJE_ID = LIST.LINJE_ID)
                                                  WHEN 1 < (SELECT COUNT(*)
                                                            FROM T_LINJE_STATUS LIS3
                                                            WHERE LIS3.OPPDRAGS_ID = LIST.OPPDRAGS_ID
                                                            AND LIS3.LINJE_ID = LIST.LINJE_ID
                                                            AND LIS3.KODE_STATUS = 'KORR')
                                                            THEN LIS1.DATO_FOM
                                                            ELSE (SELECT MAX(LIS4.DATO_FOM)
                                                                  FROM T_LINJE_STATUS LIS4
                                                                  WHERE LIS4.OPPDRAGS_ID = LIST.OPPDRAGS_ID
                                                                  AND LIS4.LINJE_ID = LIST.LINJE_ID) END) = LIS1.DATO_FOM)
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOppdragsLinje,
            )
        }
    }

    fun hentOppdragsOmposteringer(
        gjelderId: String,
        oppdragsId: Int,
    ): List<Ompostering> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OM.GJELDER_ID,
                        OM.KODE_FAGGRUPPE,
                        OM.LOPENR,
                        OM.OMPOSTERING,
                        OM.DATO_OMPOSTER_FOM,
                        OM.FEILREG,
                        OM.BEREGNINGS_ID,
                        OM.UTFORT,
                        OM.BRUKERID,
                        OM.TIDSPKT_REG
                      FROM T_OMPOSTERING OM,
                        T_OPPDRAG OP,
                        T_FAGOMRAADE FO, 
                        T_FAGGRUPPE FG
                    WHERE OM.GJELDER_ID = :gjelderId
                    AND OP.OPPDRAGS_ID = :oppdragsId
                    AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                    AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                    AND FG.KODE_FAGGRUPPE = OM.KODE_FAGGRUPPE
                    ORDER BY OM.DATO_OMPOSTER_FOM
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOppdragsOmpostering,
            )
        }
    }

    fun hentOppdragsEnhetsHistorikk(oppdragsId: Int): List<OppdragsEnhet> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TYPE_ENHET, DATO_FOM, ENHET 
                    FROM T_OPPDRAGSENHET 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    ORDER BY DATO_FOM
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOppdragsEnhet,
            )
        }
    }

    fun hentOppdragsStatusHistorikk(oppdragsId: Int): List<OppdragStatus> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT KODE_STATUS, TIDSPKT_REG, BRUKERID
                    FROM T_OPPDRAG_STATUS 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    ORDER BY TIDSPKT_REG
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOppdragsStatusHistorikk,
            )
        }
    }

    fun hentOppdragsLinjeStatuser(
        oppdragsId: Int,
        linjeId: Int,
    ): List<LinjeStatus> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT KODE_STATUS, DATO_FOM, TIDSPKT_REG, BRUKERID
                    FROM T_LINJE_STATUS 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    ORDER BY DATO_FOM
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                        "linjeId" to linjeId,
                    ),
                ),
                mapToOppdragsLinjeStatus,
            )
        }
    }

    fun hentOppdragsLinjeAttestanter(
        oppdragsId: Int,
        linjeId: Int,
    ): List<Attestant> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT ATTESTANT_ID, DATO_UGYLDIG_FOM
                    FROM T_ATTESTASJON 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    ORDER BY DATO_UGYLDIG_FOM
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                        "linjeId" to linjeId,
                    ),
                ),
                mapToOppdragsLinjeAttestant,
            )
        }
    }

    fun hentKorreksjoner(oppdragsId: String): List<Korreksjon> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, LINJE_ID_KORR
                    FROM T_KORREKSJON 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOppdragsKorreksjon,
            )
        }
    }

    fun eksistererValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(
        oppdragsId: Int,
        linjeId: Int,
    ): Map<String, Boolean> {
        val eksisterer = mutableMapOf<String, Boolean>()
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT 'T_VALUTA' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_VALUTA
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_SKYLDNER' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_SKYLDNER
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_KRAVHAVER' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_KRAVHAVER
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_LINJEENHET ' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_LINJEENHET 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_GRAD' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_GRAD
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_TEKST' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_TEKST
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_KID' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_KID
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    UNION ALL
                    SELECT 'T_MAKS_DATO' AS table_name, COUNT(*) AS EKSISTERER
                    FROM T_MAKS_DATO
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID = :linjeId
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                        "linjeId" to linjeId,
                    ),
                ),
            ) { row -> eksisterer.computeIfAbsent(row.string("TABLE_NAME")) { row.boolean("EKSISTERER") } }
            eksisterer
        }
    }

    fun hentValutaerList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Valuta> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TYPE_VALUTA, DATO_FOM, NOKKEL_ID, VALUTA, FEILREG, TIDSPKT_REG, BRUKERID
                    FROM T_VALUTA 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToValuta,
            )
        }
    }

    fun hentSkyldnereList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Skyldner> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, SKYLDNER_ID, DATO_FOM, TIDSPKT_REG, BRUKERID
                    FROM T_SKYLDNER 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToSkyldner,
            )
        }
    }

    fun hentKravhavereList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Kravhaver> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, KRAVHAVER_ID, DATO_FOM, TIDSPKT_REG, BRUKERID
                    FROM T_KRAVHAVER 
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToKravhaver,
            )
        }
    }

    fun hentEnheterList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<LinjeEnhet> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TYPE_ENHET, ENHET, DATO_FOM, NOKKEL_ID, TIDSPKT_REG, BRUKERID
                    FROM T_LINJEENHET
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToLinjeEnhet,
            )
        }
    }

    fun hentGraderList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Grad> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, TYPE_GRAD, GRAD, TIDSPKT_REG, BRUKERID
                    FROM T_GRAD
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToGrad,
            )
        }
    }

    fun hentTeksterList(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Tekst> {
        return using(sessionOf(dataSource)) { session ->
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
                mapToTekst,
            )
        }
    }

    fun hentKidListe(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Kid> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, KID, DATO_FOM, TIDSPKT_REG, BRUKERID
                    FROM T_KID
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToKid,
            )
        }
    }

    fun hentMaksdatoerListe(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Maksdato> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, MAKS_DATO, DATO_FOM, TIDSPKT_REG, BRUKERID
                    FROM T_MAKS_DATO
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToMaksDato,
            )
        }
    }

    fun hentOvrigListe(
        oppdragsId: Int,
        linjeIder: List<Int>,
    ): List<Ovrig> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT LINJE_ID, VEDTAK_ID, HENVISNING, TYPE_SOKNAD
                    FROM T_OPPDRAGSLINJE
                    WHERE OPPDRAGS_ID = :oppdragsId
                    AND LINJE_ID IN (${linjeIder.joinToString()})
                    """.trimIndent(),
                    mapOf(
                        "oppdragsId" to oppdragsId,
                    ),
                ),
                mapToOvrig,
            )
        }
    }

    private val mapToOppdragsInfo: (Row) -> OppdragsInfo = { row ->
        OppdragsInfo(
            gjelderId = row.string("OPPDRAG_GJELDER_ID"),
        )
    }

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            fagsystemId = row.string("FAGSYSTEM_ID"),
            oppdragsId = row.int("OPPDRAGS_ID"),
            navnFagGruppe = row.string("NAVN_FAGGRUPPE"),
            navnFagOmraade = row.string("NAVN_FAGOMRAADE"),
            kjorIdag = row.string("KJOR_IDAG"),
            typeBilag = row.string("TYPE_BILAG"),
            kodeStatus = row.string("KODE_STATUS"),
        )
    }

    private val mapToFagGruppe: (Row) -> FagGruppe = { row ->
        FagGruppe(
            navn = row.string("NAVN_FAGGRUPPE").trimIndent(),
            type = row.string("KODE_FAGGRUPPE").trimIndent(),
        )
    }

    private val mapToOppdragsEnhet: (Row) -> OppdragsEnhet = { row ->
        OppdragsEnhet(
            type = row.string("TYPE_ENHET"),
            datoFom = row.string("DATO_FOM"),
            enhet = row.string("ENHET"),
        )
    }

    private val mapToOppdragsLinje: (Row) -> OppdragsLinje = { row ->
        OppdragsLinje(
            linjeId = row.int("LINJE_ID"),
            kodeKlasse = row.string("KODE_KLASSE"),
            datoVedtakFom = row.string("DATO_VEDTAK_FOM"),
            datoVedtakTom = row.stringOrNull("DATO_VEDTAK_TOM"),
            sats = row.double("SATS"),
            typeSats = row.string("TYPE_SATS"),
            kodeStatus = row.string("KODE_STATUS"),
            datoFom = row.string("DATO_FOM"),
            linjeIdKorr = row.intOrNull("LINJE_ID_KORR"),
            attestert = row.string("ATTESTERT"),
            delytelseId = row.string("DELYTELSE_ID"),
            utbetalesTilId = row.string("UTBETALES_TIL_ID"),
            refunderesOrgnr = row.stringOrNull("REFUNDERES_ID"),
            brukerId = row.string("BRUKERID"),
            tidspktReg = row.string("TIDSPKT_REG"),
        )
    }

    private val mapToOppdragsOmpostering: (Row) -> Ompostering = { row ->
        Ompostering(
            id = row.string("GJELDER_ID"),
            kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
            lopenr = row.int("LOPENR"),
            ompostering = row.string("OMPOSTERING"),
            omposteringFom = row.stringOrNull("DATO_OMPOSTER_FOM"),
            feilReg = row.string("FEILREG"),
            beregningsId = row.intOrNull("BEREGNINGS_ID"),
            utfort = row.string("UTFORT"),
            brukerid = row.string("BRUKERID"),
            tidspktReg = row.string("TIDSPKT_REG"),
        )
    }

    private val mapToOppdragsStatusHistorikk: (Row) -> OppdragStatus = { row ->
        OppdragStatus(
            kodeStatus = row.string("KODE_STATUS"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToOppdragsLinjeStatus: (Row) -> LinjeStatus = { row ->
        LinjeStatus(
            status = row.string("KODE_STATUS"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToOppdragsLinjeAttestant: (Row) -> Attestant = { row ->
        Attestant(
            attestantId = row.string("ATTESTANT_ID"),
            ugyldigFom = row.string("DATO_UGYLDIG_FOM"),
        )
    }

    private val mapToOppdragsKorreksjon: (Row) -> Korreksjon = { row ->
        Korreksjon(
            linje = row.int("LINJE_ID"),
            korrigertLinje = row.int("LINJE_ID_KORR"),
        )
    }

    private val mapToValuta: (Row) -> Valuta = { row ->
        Valuta(
            linjeId = row.int("LINJE_ID"),
            type = row.string("TYPE_VALUTA"),
            datoFom = row.string("DATO_FOM"),
            nokkelId = row.int("NOKKEL_ID"),
            valuta = row.string("VALUTA"),
            feilreg = row.stringOrNull("FEILREG"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToSkyldner: (Row) -> Skyldner = { row ->
        Skyldner(
            linjeId = row.int("LINJE_ID"),
            skyldnerId = row.string("SKYLDNER_ID"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToKravhaver: (Row) -> Kravhaver = { row ->
        Kravhaver(
            linjeId = row.int("LINJE_ID"),
            kravhaverId = row.string("KRAVHAVER_ID"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToLinjeEnhet: (Row) -> LinjeEnhet = { row ->
        LinjeEnhet(
            linjeId = row.int("LINJE_ID"),
            typeEnhet = row.string("TYPE_ENHET"),
            enhet = row.stringOrNull("ENHET"),
            datoFom = row.string("DATO_FOM"),
            nokkelId = row.int("NOKKEL_ID"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToGrad: (Row) -> Grad = { row ->
        Grad(
            linjeId = row.int("LINJE_ID"),
            typeGrad = row.string("TYPE_GRAD"),
            grad = row.int("GRAD"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToTekst: (Row) -> Tekst = { row ->
        Tekst(
            linjeId = row.int("LINJE_ID"),
            tekst = row.string("TEKST"),
        )
    }

    private val mapToKid: (Row) -> Kid = { row ->
        Kid(
            linjeId = row.int("LINJE_ID"),
            kid = row.string("KID"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToMaksDato: (Row) -> Maksdato = { row ->
        Maksdato(
            linjeId = row.int("LINJE_ID"),
            maksdato = row.string("MAKS_DATO"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToOvrig: (Row) -> Ovrig = { row ->
        Ovrig(
            linjeId = row.int("LINJE_ID"),
            vedtaksId = row.string("VEDTAK_ID"),
            henvisning = row.string("HENVISNING"),
            soknadsType = row.string("TYPE_SOKNAD"),
        )
    }
}
