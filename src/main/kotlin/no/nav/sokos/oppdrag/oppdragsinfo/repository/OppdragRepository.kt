package no.nav.sokos.oppdrag.oppdragsinfo.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Korreksjon
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsStatus

class OppdragRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getOppdrag(
        gjelderId: String,
        fagGruppeKode: String?,
    ): List<Oppdrag> =
        using(sessionOf(dataSource)) { session ->
            val sql =
                """
                SELECT OP.OPPDRAGS_ID,
                    TRIM(OP.FAGSYSTEM_ID) AS FAGSYSTEM_ID,
                    TRIM(FO.NAVN_FAGOMRAADE) AS NAVN_FAGOMRAADE,
                    OP.OPPDRAG_GJELDER_ID,
                    OP.KJOR_IDAG,
                    TRIM(OP.TYPE_BILAG) AS TYPE_BILAG,
                    TRIM(FG.NAVN_FAGGRUPPE) AS NAVN_FAGGRUPPE,
                    OS.KODE_STATUS
                FROM T_OPPDRAG OP,
                    T_FAGOMRAADE FO, 
                    T_FAGGRUPPE FG,
                    T_OPPDRAG_STATUS OS
                WHERE OP.OPPDRAG_GJELDER_ID = :gjelderId
                    ${if (fagGruppeKode?.isEmpty() == false) " AND FG.KODE_FAGGRUPPE = :fagGruppeKode" else ""}
                AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                AND OS.OPPDRAGS_ID = OP.OPPDRAGS_ID
                AND OS.TIDSPKT_REG = (
                SELECT MAX(OS2.TIDSPKT_REG)
                FROM T_OPPDRAG_STATUS OS2
                WHERE OS2.OPPDRAGS_ID = OS.OPPDRAGS_ID)
                ORDER BY OS.KODE_STATUS
                """.trimIndent()

            session.list(
                queryOf(
                    sql,
                    mapOf(
                        "gjelderId" to gjelderId,
                        "fagGruppeKode" to fagGruppeKode,
                    ),
                ),
                mapToOppdrag,
            )
        }

    fun getOppdragsEnhet(
        typeEnhet: String? = null,
        oppdragsId: Int,
    ): List<OppdragsEnhet> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(TYPE_ENHET) AS TYPE_ENHET, DATO_FOM, TRIM(ENHET) AS ENHET 
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

    fun getOppdragsLinjer(oppdragsId: Int): List<OppdragsLinje> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OPLI.LINJE_ID,
                        TRIM(OPLI.KODE_KLASSE) AS KODE_KLASSE,
                        OPLI.DATO_VEDTAK_FOM,
                        OPLI.DATO_VEDTAK_TOM,
                        OPLI.SATS,
                        TRIM(OPLI.TYPE_SATS) AS TYPE_SATS,
                        LIST.KODE_STATUS,
                        LIST.DATO_FOM,
                        OPLI.ATTESTERT,
                        KORR.LINJE_ID_KORR,
                        TRIM(OPLI.DELYTELSE_ID) AS DELYTELSE_ID,
                        OPLI.UTBETALES_TIL_ID,
                        TRIM(OPLI.REFUNDERES_ID) AS REFUNDERES_ID,
                        TRIM(OPLI.BRUKERID) AS BRUKERID,
                        OPLI.TIDSPKT_REG,
                        VEDTAKSSATS.VEDTAKSSATS AS VEDTAKSSATS,
                        KONT.HOVEDKONTONR AS HOVEDKONTONR,
                        KONT.UNDERKONTONR AS UNDERKONTONR
                    FROM T_KJOREDATO KJDA, T_OPPDRAGSLINJE OPLI, T_KONTOREGEL KONT, T_LINJE_STATUS LIST
                    LEFT OUTER JOIN T_KORREKSJON KORR
                        ON LIST.OPPDRAGS_ID = KORR.OPPDRAGS_ID
                        AND LIST.LINJE_ID = KORR.LINJE_ID
                    LEFT OUTER JOIN T_LINJE_VEDTAKSSATS VEDTAKSSATS
                        ON LIST.OPPDRAGS_ID = VEDTAKSSATS.OPPDRAGS_ID 
                        AND LIST.LINJE_ID = VEDTAKSSATS.LINJE_ID
                    WHERE OPLI.OPPDRAGS_ID = :oppdragsId
                    AND KONT.KODE_KLASSE = OPLI.KODE_KLASSE
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

    fun getOppdragsOmposteringer(oppdragsId: Int): List<Ompostering> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OM.GJELDER_ID,
                        TRIM(OM.KODE_FAGGRUPPE) AS KODE_FAGGRUPPE,
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
                    WHERE OP.OPPDRAGS_ID = :oppdragsId
                    AND OM.GJELDER_ID = OP.OPPDRAG_GJELDER_ID
                    AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                    AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                    AND FG.KODE_FAGGRUPPE = OM.KODE_FAGGRUPPE
                    ORDER BY OM.DATO_OMPOSTER_FOM
                    """.trimIndent(),
                    mapOf("oppdragsId" to oppdragsId),
                ),
                mapToOppdragsOmpostering,
            )
        }

    fun getOppdragsEnhetsHistorikk(oppdragsId: Int): List<OppdragsEnhet> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(TYPE_ENHET) AS TYPE_ENHET, DATO_FOM, TRIM(ENHET) AS ENHET 
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

    fun getOppdragsStatusHistorikk(oppdragsId: Int): List<OppdragsStatus> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT KODE_STATUS, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
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

    fun getOppdragsLinjeStatuser(
        oppdragsId: Int,
        linjeId: Int,
    ): List<LinjeStatus> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(KODE_STATUS) AS KODE_STATUS, DATO_FOM, TIDSPKT_REG, TRIM(BRUKERID) AS BRUKERID
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

    fun getOppdragsLinjeAttestanter(
        oppdragsId: Int,
        linjeId: Int,
    ): List<Attestant> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(ATTESTANT_ID) AS ATTESTANT_ID, DATO_UGYLDIG_FOM
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

    fun getKorreksjoner(oppdragsId: String): List<Korreksjon> =
        using(sessionOf(dataSource)) { session ->
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

    fun existsValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(
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
                    SELECT 'T_LINJEENHET' AS table_name, COUNT(*) AS EKSISTERER
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

    private val mapToOppdrag: (Row) -> Oppdrag = { row ->
        Oppdrag(
            fagsystemId = row.string("FAGSYSTEM_ID"),
            oppdragsId = row.int("OPPDRAGS_ID"),
            navnFaggruppe = row.string("NAVN_FAGGRUPPE"),
            navnFagomraade = row.string("NAVN_FAGOMRAADE"),
            kjorIdag = row.string("KJOR_IDAG"),
            typeBilag = row.string("TYPE_BILAG"),
            kodeStatus = row.string("KODE_STATUS"),
        )
    }

    private val mapToOppdragsEnhet: (Row) -> OppdragsEnhet = { row ->
        OppdragsEnhet(
            typeEnhet = row.string("TYPE_ENHET"),
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
            refunderesId = row.stringOrNull("REFUNDERES_ID"),
            vedtakssats = row.doubleOrNull("VEDTAKSSATS"),
            brukerId = row.string("BRUKERID"),
            tidspktReg = row.string("TIDSPKT_REG"),
            hovedkontonr = row.stringOrNull("HOVEDKONTONR"),
            underkontonr = row.stringOrNull("UNDERKONTONR"),
        )
    }

    private val mapToOppdragsOmpostering: (Row) -> Ompostering = { row ->
        Ompostering(
            gjelderId = row.string("GJELDER_ID"),
            kodeFaggruppe = row.string("KODE_FAGGRUPPE"),
            lopenr = row.int("LOPENR"),
            ompostering = row.string("OMPOSTERING"),
            datoOmposterFom = row.stringOrNull("DATO_OMPOSTER_FOM"),
            feilReg = row.string("FEILREG"),
            beregningsId = row.intOrNull("BEREGNINGS_ID"),
            utfort = row.string("UTFORT"),
            brukerid = row.string("BRUKERID"),
            tidspktReg = row.string("TIDSPKT_REG"),
        )
    }

    private val mapToOppdragsStatusHistorikk: (Row) -> OppdragsStatus = { row ->
        OppdragsStatus(
            kodeStatus = row.string("KODE_STATUS"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToOppdragsLinjeStatus: (Row) -> LinjeStatus = { row ->
        LinjeStatus(
            kodeStatus = row.string("KODE_STATUS"),
            datoFom = row.string("DATO_FOM"),
            tidspktReg = row.string("TIDSPKT_REG"),
            brukerid = row.string("BRUKERID"),
        )
    }

    private val mapToOppdragsLinjeAttestant: (Row) -> Attestant = { row ->
        Attestant(
            attestantId = row.string("ATTESTANT_ID"),
            datoUgyldigFom = row.string("DATO_UGYLDIG_FOM"),
        )
    }

    private val mapToOppdragsKorreksjon: (Row) -> Korreksjon = { row ->
        Korreksjon(
            linjeId = row.int("LINJE_ID"),
            linjeIdKorr = row.int("LINJE_ID_KORR"),
        )
    }
}
