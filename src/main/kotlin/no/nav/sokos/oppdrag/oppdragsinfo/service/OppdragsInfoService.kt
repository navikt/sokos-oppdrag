package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.model.Attestant
import no.nav.sokos.oppdrag.common.model.FagGruppe
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinjeDetaljer
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsinfoTreffliste
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class OppdragsInfoService(
    private val oppdragsInfoRepository: OppdragsInfoRepository = OppdragsInfoRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    fun sokOppdragsInfo(
        gjelderId: String,
        faggruppeKode: String?,
        applicationCall: ApplicationCall,
    ): List<OppdragsinfoTreffliste> {
        val navIdent = getSaksbehandler(applicationCall)

        secureLogger.info { "Søker etter oppdrag med gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = navIdent.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på OppdragsInfo",
            ),
        )

        val oppdragsInfo = oppdragsInfoRepository.hentOppdragsInfo(gjelderId)

        if (oppdragsInfo == null) {
            secureLogger.info { "Fant ingen oppdrag for gjelderId: $gjelderId" }
            return emptyList()
        } else {
            val oppdragsListe =
                oppdragsInfoRepository.hentOppdragsListe(oppdragsInfo.gjelderId!!, faggruppeKode)
            return listOf(
                OppdragsinfoTreffliste(
                    gjelderId = oppdragsInfo.gjelderId,
                    oppdragsListe = oppdragsListe,
                ),
            )
        }
    }

    fun hentFaggrupper(): List<FagGruppe> {
        logger.info { "Henter faggrupper" }
        return oppdragsInfoRepository.hentFagGrupper()
    }

    fun hentOppdrag(
        gjelderId: String,
        oppdragsId: Int,
    ): Oppdrag {
        logger.info { "Henter oppdragslinjer med oppdragsId: $oppdragsId" }

        val oppdragTilknyttetBruker = oppdragsInfoRepository.erOppdragTilknyttetBruker(gjelderId, oppdragsId)

        if (!oppdragTilknyttetBruker) {
            logger.error { "Oppdraget med id: $oppdragsId er ikke knyttet til bruker" }
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Oppdraget er ikke knyttet til bruker"),
            )
        }

        val enhet = oppdragsInfoRepository.hentOppdragsEnhet(oppdragsId = oppdragsId).first()
        val behandlendeEnhet = oppdragsInfoRepository.hentOppdragsEnhet("BEH", oppdragsId).firstOrNull()
        val harOmposteringer = oppdragsInfoRepository.eksistererOmposteringer(gjelderId, oppdragsId)
        val oppdragsLinjer = oppdragsInfoRepository.hentOppdragsLinjer(oppdragsId)

        return Oppdrag(
            enhet,
            behandlendeEnhet,
            harOmposteringer,
            oppdragsLinjer,
        )
    }

    fun hentOppdragsOmposteringer(
        gjelderId: String,
        oppdragsId: String,
    ): List<Ompostering> {
        logger.info { "Henter omposteringer for oppdragsId: $oppdragsId" }
        return oppdragsInfoRepository.hentOppdragsOmposteringer(gjelderId, oppdragsId.toInt())
    }

    fun hentOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> {
        logger.info { "Henter enhetshistorikk for oppdragId: $oppdragsId" }
        return oppdragsInfoRepository.hentOppdragsEnhetsHistorikk(oppdragsId.toInt())
    }

    fun hentOppdragsStatusHistorikk(oppdragsId: String): List<OppdragStatus> {
        logger.info { "Henter statushistorikk for oppdragId: $oppdragsId" }
        return oppdragsInfoRepository.hentOppdragsStatusHistorikk(oppdragsId.toInt())
    }

    fun hentOppdragsLinjeStatuser(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeStatus> {
        logger.info { "Henter linjstatus for oppdrag: $oppdragsId, linje: $linjeId" }
        return oppdragsInfoRepository.hentOppdragsLinjeStatuser(oppdragsId.toInt(), linjeId.toInt())
    }

    fun hentOppdragsLinjeAttestanter(
        oppdragsId: String,
        linjeId: String,
    ): List<Attestant> {
        logger.info { "Henter attestant for oppdrag: $oppdragsId, linje : $linjeId" }
        return oppdragsInfoRepository.hentOppdragsLinjeAttestanter(oppdragsId.toInt(), linjeId.toInt())
    }

    fun hentOppdragsLinjeDetaljer(
        oppdragsId: String,
        linjeId: String,
    ): OppdragsLinjeDetaljer {
        logger.info { "Henter detaljer for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        val eksisterer = oppdragsInfoRepository.eksistererValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(oppdragsId.toInt(), linjeId.toInt())
        return OppdragsLinjeDetaljer(
            korrigerteLinjeIder = korrigerteLinjeIder,
            harValutaer = eksisterer["T_VALUTA"] ?: false,
            harSkyldnere = eksisterer["T_SKYLDNER"] ?: false,
            harKravhavere = eksisterer["T_KRAVHAVER"] ?: false,
            harEnheter = eksisterer["T_ENHET"] ?: false,
            harGrader = eksisterer["T_GRAD"] ?: false,
            harTekster = eksisterer["T_TEKST"] ?: false,
            harKidliste = eksisterer["T_KID"] ?: false,
            harMaksdatoer = eksisterer["T_MAKS_DATO"] ?: false,
        )
    }

    fun hentOppdragsLinjeValuta(
        oppdragsId: String,
        linjeId: String,
    ): List<Valuta> {
        logger.info { "Henter valuta for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentValutaerList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeSkyldner(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        logger.info { "Henter skyldner for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentSkyldnereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKravhaver(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        logger.info { "Henter kravhaver for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKravhavereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        logger.info { "Henter enheter liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentEnheterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeGrad(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        logger.info { "Henter grad for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentGraderList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeTekst(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        logger.info { "Henter tekst liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentTeksterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKidListe(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        logger.info { "Henter kid for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKidListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeMaksdato(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        logger.info { "Henter maksdato for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentMaksdatoerListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeOvrig(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        logger.info { "Henter øvrig for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentOvrigListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    private fun finnKorrigerteLinjer(
        oppdragsId: String,
        linjeId: String,
    ): List<Int> {
        val korrigerteLinjer = oppdragsInfoRepository.hentKorreksjoner(oppdragsId)
        val korrigerteLinjeIder: MutableList<Int> = ArrayList()
        if (korrigerteLinjer.isNotEmpty()) {
            var linje = linjeId.toInt()
            for (korreksjon in korrigerteLinjer) {
                val korrLinje = korreksjon.linje
                if (korrLinje == linje) {
                    korrigerteLinjeIder.add(korrLinje)
                    linje = korreksjon.korrigertLinje
                }
            }
            korrigerteLinjeIder.add(linje)
        } else {
            korrigerteLinjeIder.add(linjeId.toInt())
        }
        return korrigerteLinjeIder
    }
}
