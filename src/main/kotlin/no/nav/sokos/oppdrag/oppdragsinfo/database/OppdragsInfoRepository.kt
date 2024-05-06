package no.nav.sokos.oppdrag.oppdragsinfo.database

import no.nav.sokos.oppdrag.oppdragsinfo.database.RepositoryExtensions.getColumn
import no.nav.sokos.oppdrag.oppdragsinfo.database.RepositoryExtensions.param
import no.nav.sokos.oppdrag.oppdragsinfo.database.RepositoryExtensions.toList
import no.nav.sokos.oppdrag.oppdragsinfo.database.RepositoryExtensions.withParameters
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
import java.sql.Connection
import java.sql.ResultSet

object OppdragsInfoRepository {
    fun Connection.hentOppdragsInfo(gjelderId: String): List<OppdragsInfo> =
        prepareStatement(
            """
            SELECT OPPDRAG_GJELDER_ID
            FROM T_OPPDRAG
            WHERE OPPDRAG_GJELDER_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(gjelderId),
        ).run {
            executeQuery().toOppdrag()
        }

    fun Connection.erOppdragTilknyttetBruker(
        gjelderId: String,
        oppdragsId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)
            FROM T_OPPDRAG
            WHERE OPPDRAG_GJELDER_ID = (?)
            AND OPPDRAGS_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(gjelderId),
            param(oppdragsId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.hentOppdragsListe(
        gjelderId: String,
        faggruppeKode: String?,
    ): List<Oppdrag> =
        prepareStatement(
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
            WHERE OP.OPPDRAG_GJELDER_ID = (?)
            ${if (faggruppeKode != null) " AND FG.KODE_FAGGRUPPE = (?)" else ""}
            AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
            AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
            AND OS.OPPDRAGS_ID = OP.OPPDRAGS_ID
            AND OS.TIDSPKT_REG = (
            SELECT MAX(OS2.TIDSPKT_REG)
            FROM T_OPPDRAG_STATUS OS2
            WHERE OS2.OPPDRAGS_ID = OS.OPPDRAGS_ID)
            ORDER BY OS.KODE_STATUS
            """.trimIndent(),
        ).withParameters(
            param(gjelderId),
            faggruppeKode?.let { param(faggruppeKode) },
        ).run {
            return executeQuery().toOppdragsListe()
        }

    fun Connection.eksistererOmposteringer(
        gjelderId: String,
        oppdragsId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)
            FROM T_OMPOSTERING OM,
                    T_OPPDRAG OP,
                    T_FAGOMRAADE FO, 
                    T_FAGGRUPPE FG
            WHERE OM.GJELDER_ID = (?)
            AND OP.OPPDRAGS_ID = (?)
            AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
            AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
            AND FG.KODE_FAGGRUPPE = OM.KODE_FAGGRUPPE
            """.trimIndent(),
        ).withParameters(
            param(gjelderId),
            param(oppdragsId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.hentFaggrupper(): List<FagGruppe> =
        prepareStatement(
            """
            SELECT NAVN_FAGGRUPPE, KODE_FAGGRUPPE
            FROM T_FAGGRUPPE 
            ORDER BY NAVN_FAGGRUPPE
            """.trimIndent(),
        ).run {
            executeQuery().toFagGrupper()
        }

    fun Connection.hentOppdragsOmposteringer(
        gjelderId: String,
        oppdragsId: Int,
    ): List<Ompostering> =
        prepareStatement(
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
            FROM    T_OMPOSTERING OM,
                    T_OPPDRAG OP,
                    T_FAGOMRAADE FO, 
                    T_FAGGRUPPE FG
            WHERE OM.GJELDER_ID = (?)
            AND OP.OPPDRAGS_ID = (?)
            AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
            AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
            AND FG.KODE_FAGGRUPPE = OM.KODE_FAGGRUPPE
            ORDER BY OM.DATO_OMPOSTER_FOM
            """.trimIndent(),
        ).withParameters(
            param(gjelderId),
            param(oppdragsId),
        ).run {
            executeQuery().toOppdragsOmposteringer()
        }

    fun Connection.hentOppdragsLinjer(oppdragId: Int): List<OppdragsLinje> =
        prepareStatement(
            """
            SELECT 
                OPLI.LINJE_ID,
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
                WHERE OPLI.OPPDRAGS_ID = (?)
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
        ).withParameters(
            param(oppdragId),
        ).run {
            executeQuery().toOppdragsLinjer()
        }

    fun Connection.hentOppdragsEnhet(
        typeEnhet: String? = null,
        oppdragsId: Int,
    ): List<OppdragsEnhet> =
        prepareStatement(
            """
            SELECT TYPE_ENHET, DATO_FOM, ENHET 
            FROM T_OPPDRAGSENHET 
            WHERE OPPDRAGS_ID = (?)
            ${if (typeEnhet != null) " AND TYPE_ENHET = (?)" else " AND TYPE_ENHET IN (SELECT TYPE_ENHET FROM T_ENHETSTYPE WHERE TYPE_ENHET != 'BEH')"}
            AND DATO_FOM = (SELECT MAX(DATO_FOM) 
                            FROM T_OPPDRAGSENHET
                            WHERE OPPDRAGS_ID = (?)
                            ${if (typeEnhet != null) " AND TYPE_ENHET = (?))" else " AND TYPE_ENHET IN (SELECT TYPE_ENHET FROM T_ENHETSTYPE WHERE TYPE_ENHET != 'BEH'))"}
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            typeEnhet?.let { param(typeEnhet) },
            param(oppdragsId),
            typeEnhet?.let { param(typeEnhet) },
        ).run {
            executeQuery().toOppdragsEnhet()
        }

    fun Connection.hentOppdragsEnhetsHistorikk(oppdragsId: Int): List<OppdragsEnhet> =
        prepareStatement(
            """
            SELECT TYPE_ENHET, DATO_FOM, ENHET 
            FROM T_OPPDRAGSENHET 
            WHERE OPPDRAGS_ID = (?)
            ORDER BY DATO_FOM
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toOppdragsEnhet()
        }

    fun Connection.hentOppdragsStatusHistorikk(oppdragsId: Int): List<OppdragStatus> =
        prepareStatement(
            """
            SELECT KODE_STATUS, TIDSPKT_REG, BRUKERID
            FROM T_OPPDRAG_STATUS 
            WHERE OPPDRAGS_ID = (?)
            ORDER BY TIDSPKT_REG
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toOppdragsStatusHistorikk()
        }

    fun Connection.hentOppdragsLinjeStatuser(
        oppdragsId: Int,
        linjeId: Int,
    ): List<LinjeStatus> =
        prepareStatement(
            """
            SELECT KODE_STATUS, DATO_FOM, TIDSPKT_REG, BRUKERID
            FROM T_LINJE_STATUS 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            ORDER BY DATO_FOM
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().toOppdragsLinjeStatuser()
        }

    fun Connection.hentOppdragsLinjeAttestanter(
        oppdragsId: Int,
        linjeId: Int,
    ): List<Attestant> =
        prepareStatement(
            """
            SELECT ATTESTANT_ID, DATO_UGYLDIG_FOM
            FROM T_ATTESTASJON 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            ORDER BY DATO_UGYLDIG_FOM
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().toOppdragsLinjeAttestanter()
        }

    fun Connection.hentKorreksjoner(oppdragsId: String): List<Korreksjon> =
        prepareStatement(
            """
            SELECT LINJE_ID, LINJE_ID_KORR
            FROM T_KORREKSJON 
            WHERE OPPDRAGS_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toOppdragsKorreksjoner()
        }

    fun Connection.eksistererValutaer(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_VALUTA 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererSkyldnere(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_SKYLDNER 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererKravhavere(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_KRAVHAVER 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererEnheter(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_LINJEENHET 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererGrader(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_GRAD 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererTekster(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_TEKST 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererKidliste(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_KID 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.eksistererMaksdatoer(
        oppdragsId: Int,
        linjeId: Int,
    ): Boolean =
        prepareStatement(
            """
            SELECT COUNT(*)  
            FROM T_MAKS_DATO  
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID = (?)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
            param(linjeId),
        ).run {
            executeQuery().let { it.next() && it.getInt(1) > 0 }
        }

    fun Connection.hentValutaer(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Valuta> =
        prepareStatement(
            """
            SELECT LINJE_ID, TYPE_VALUTA, DATO_FOM, NOKKEL_ID, VALUTA, FEILREG, TIDSPKT_REG, BRUKERID
            FROM T_VALUTA 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toValuta()
        }

    fun Connection.hentSkyldnere(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Skyldner> =
        prepareStatement(
            """
            SELECT LINJE_ID, SKYLDNER_ID, DATO_FOM, TIDSPKT_REG, BRUKERID
            FROM T_SKYLDNER 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toSkyldner()
        }

    fun Connection.hentKravhavere(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Kravhaver> =
        prepareStatement(
            """
            SELECT LINJE_ID, KRAVHAVER_ID, DATO_FOM, TIDSPKT_REG, BRUKERID
            FROM T_KRAVHAVER 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toKravhaver()
        }

    fun Connection.hentEnheter(
        oppdragsId: Int,
        linjeIder: String,
    ): List<LinjeEnhet> =
        prepareStatement(
            """
            SELECT LINJE_ID, TYPE_ENHET, ENHET, DATO_FOM, NOKKEL_ID, TIDSPKT_REG, BRUKERID
            FROM T_LINJEENHET 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toEnheter()
        }

    fun Connection.hentGrader(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Grad> =
        prepareStatement(
            """
            SELECT LINJE_ID, TYPE_GRAD, GRAD, TIDSPKT_REG, BRUKERID
            FROM T_GRAD 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toGrad()
        }

    fun Connection.hentTekster(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Tekst> =
        prepareStatement(
            """
            SELECT  LINJE_ID, TEKST
            FROM T_TEKST 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toTekster()
        }

    fun Connection.hentKidliste(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Kid> =
        prepareStatement(
            """
            SELECT LINJE_ID, KID, DATO_FOM, TIDSPKT_REG, BRUKERID
            FROM T_KID 
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toLKidlist()
        }

    fun Connection.hentMaksdatoer(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Maksdato> =
        prepareStatement(
            """
            SELECT LINJE_ID, MAKS_DATO, DATO_FOM, TIDSPKT_REG, BRUKERID
            FROM T_MAKS_DATO  
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toMaksdato()
        }

    fun Connection.hentOvrige(
        oppdragsId: Int,
        linjeIder: String,
    ): List<Ovrig> =
        prepareStatement(
            """
            SELECT LINJE_ID, VEDTAK_ID, HENVISNING, TYPE_SOKNAD
            FROM T_OPPDRAGSLINJE  
            WHERE OPPDRAGS_ID = (?)
            AND LINJE_ID IN ($linjeIder)
            """.trimIndent(),
        ).withParameters(
            param(oppdragsId),
        ).run {
            executeQuery().toOvrig()
        }

    private fun ResultSet.toOppdrag() =
        toList {
            OppdragsInfo(
                gjelderId = getColumn("OPPDRAG_GJELDER_ID"),
            )
        }

    private fun ResultSet.toOppdragsListe() =
        toList {
            Oppdrag(
                fagsystemId = getColumn("FAGSYSTEM_ID"),
                oppdragsId = getColumn("OPPDRAGS_ID"),
                navnFagGruppe = getColumn("NAVN_FAGGRUPPE"),
                navnFagOmraade = getColumn("NAVN_FAGOMRAADE"),
                kjorIdag = getColumn("KJOR_IDAG"),
                typeBilag = getColumn("TYPE_BILAG"),
                kodeStatus = getColumn("KODE_STATUS"),
            )
        }

    private fun ResultSet.toFagGrupper() =
        toList {
            FagGruppe(
                navn = getColumn("NAVN_FAGGRUPPE"),
                type = getColumn("KODE_FAGGRUPPE"),
            )
        }

    private fun ResultSet.toOppdragsOmposteringer() =
        toList {
            Ompostering(
                id = getColumn("GJELDER_ID"),
                kodeFaggruppe = getColumn("KODE_FAGGRUPPE"),
                lopenr = getColumn("LOPENR"),
                ompostering = getColumn("OMPOSTERING"),
                omposteringFom = getColumn("DATO_OMPOSTER_FOM"),
                feilReg = getColumn("FEILREG"),
                beregningsId = getColumn("BEREGNINGS_ID"),
                utfort = getColumn("UTFORT"),
                brukerid = getColumn("BRUKERID"),
                tidspktReg = getColumn("TIDSPKT_REG"),
            )
        }

    private fun ResultSet.toOppdragsLinjer() =
        toList {
            OppdragsLinje(
                linjeId = getColumn("LINJE_ID"),
                kodeKlasse = getColumn("KODE_KLASSE"),
                datoVedtakFom = getColumn("DATO_VEDTAK_FOM"),
                datoVedtakTom = getColumn("DATO_VEDTAK_TOM"),
                sats = getColumn("SATS"),
                typeSats = getColumn("TYPE_SATS"),
                kodeStatus = getColumn("KODE_STATUS"),
                datoFom = getColumn("DATO_FOM"),
                linjeIdKorr = getColumn("LINJE_ID_KORR"),
                attestert = getColumn("ATTESTERT"),
                delytelseId = getColumn("DELYTELSE_ID"),
                utbetalesTilId = getColumn("UTBETALES_TIL_ID"),
                refunderesOrgnr = getColumn("REFUNDERES_ID"),
                brukerId = getColumn("BRUKERID"),
                tidspktReg = getColumn("TIDSPKT_REG"),
            )
        }

    private fun ResultSet.toOppdragsEnhet() =
        toList {
            OppdragsEnhet(
                type = getColumn("TYPE_ENHET"),
                datoFom = getColumn("DATO_FOM"),
                enhet = getColumn("ENHET"),
            )
        }

    private fun ResultSet.toOppdragsStatusHistorikk() =
        toList {
            OppdragStatus(
                kodeStatus = getColumn("KODE_STATUS"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toOppdragsLinjeStatuser() =
        toList {
            LinjeStatus(
                status = getColumn("KODE_STATUS"),
                datoFom = getColumn("DATO_FOM"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toOppdragsLinjeAttestanter() =
        toList {
            Attestant(
                attestantId = getColumn("ATTESTANT_ID"),
                ugyldigFom = getColumn("DATO_UGYLDIG_FOM"),
            )
        }

    private fun ResultSet.toOppdragsKorreksjoner() =
        toList {
            Korreksjon(
                linje = getColumn("LINJE_ID"),
                korrigertLinje = getColumn("LINJE_ID_KORR"),
            )
        }

    private fun ResultSet.toValuta() =
        toList {
            Valuta(
                linjeId = getColumn("LINJE_ID"),
                type = getColumn("TYPE_VALUTA"),
                datoFom = getColumn("DATO_FOM"),
                nokkelId = getColumn("NOKKEL_ID"),
                valuta = getColumn("VALUTA"),
                feilreg = getColumn("FEILREG"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toSkyldner() =
        toList {
            Skyldner(
                linjeId = getColumn("LINJE_ID"),
                skyldnerId = getColumn("SKYLDNER_ID"),
                datoFom = getColumn("DATO_FOM"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toKravhaver() =
        toList {
            Kravhaver(
                linjeId = getColumn("LINJE_ID"),
                kravhaverId = getColumn("KRAVHAVER_ID"),
                datoFom = getColumn("DATO_FOM"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toEnheter() =
        toList {
            LinjeEnhet(
                linjeId = getColumn("LINJE_ID"),
                typeEnhet = getColumn("TYPE_ENHET"),
                enhet = getColumn("ENHET"),
                datoFom = getColumn("DATO_FOM"),
                nokkelId = getColumn("NOKKEL_ID"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toGrad() =
        toList {
            Grad(
                linjeId = getColumn("LINJE_ID"),
                typeGrad = getColumn("TYPE_GRAD"),
                grad = getColumn("GRAD"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toTekster() =
        toList {
            Tekst(
                linjeId = getColumn("LINJE_ID"),
                tekst = getColumn("TEKST"),
            )
        }

    private fun ResultSet.toLKidlist() =
        toList {
            Kid(
                linjeId = getColumn("LINJE_ID"),
                kid = getColumn("KID"),
                datoFom = getColumn("DATO_FOM"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toMaksdato() =
        toList {
            Maksdato(
                linjeId = getColumn("LINJE_ID"),
                maksdato = getColumn("MAKS_DATO"),
                datoFom = getColumn("DATO_FOM"),
                tidspktReg = getColumn("TIDSPKT_REG"),
                brukerid = getColumn("BRUKERID"),
            )
        }

    private fun ResultSet.toOvrig() =
        toList {
            Ovrig(
                linjeId = getColumn("LINJE_ID"),
                vedtaksId = getColumn("VEDTAK_ID"),
                henvisning = getColumn("HENVISNING"),
                soknadsType = getColumn("TYPE_SOKNAD"),
            )
        }
}
