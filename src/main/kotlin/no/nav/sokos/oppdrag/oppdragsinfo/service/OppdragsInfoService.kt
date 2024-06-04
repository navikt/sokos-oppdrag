package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.Saksbehandler
import no.nav.sokos.oppdrag.common.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.common.security.JwtClaimHandler.getSaksbehandler
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragDetaljer
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinjeDetaljer
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository

private val logger = KotlinLogging.logger {}
val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class OppdragsInfoService(
    private val oppdragsInfoRepository: OppdragsInfoRepository = OppdragsInfoRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    fun sokOppdrag(
        gjelderId: String,
        faggruppeKode: String?,
        applicationCall: ApplicationCall,
    ): List<OppdragsInfo> {
        val saksbehandler = hentSaksbehandler(applicationCall)

        secureLogger.info { "Søker etter oppdrag med gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                saksbehandler = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på oppdrag",
            ),
        )

        val oppdragsInfo = oppdragsInfoRepository.hentOppdragsInfo(gjelderId)

        if (oppdragsInfo == null) {
            return emptyList()
        } else {
            val oppdragsListe =
                oppdragsInfoRepository.hentOppdragsListe(oppdragsInfo.gjelderId!!, faggruppeKode)
            return listOf(
                OppdragsInfo(
                    gjelderId = oppdragsInfo.gjelderId,
                    oppdragsListe = oppdragsListe,
                ),
            )
        }
    }

    fun hentFaggrupper(): List<FagGruppe> {
        return oppdragsInfoRepository.hentFagGrupper()
    }

    fun hentOppdrag(
        gjelderId: String,
        oppdragsId: Int,
    ): OppdragDetaljer {
        secureLogger.info("Henter oppdragslinjer med oppdragsId: $oppdragsId")

        val oppdragTilknyttetBruker = oppdragsInfoRepository.erOppdragTilknyttetBruker(gjelderId, oppdragsId)

        if (!oppdragTilknyttetBruker) {
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Oppdraget er ikke knyttet til bruker"),
            )
        }

        val enhet = oppdragsInfoRepository.hentOppdragsEnhet(oppdragsId = oppdragsId).first()
        val behandlendeEnhet = oppdragsInfoRepository.hentOppdragsEnhet("BEH", oppdragsId).firstOrNull()
        val harOmposteringer = oppdragsInfoRepository.eksistererOmposteringer(gjelderId, oppdragsId)
        val oppdragsLinjer = oppdragsInfoRepository.hentOppdragsLinjer(oppdragsId)

        return OppdragDetaljer(
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
        secureLogger.info("Henter omposteringer for gjelderId: $gjelderId")
        return oppdragsInfoRepository.hentOppdragsOmposteringer(gjelderId, oppdragsId.toInt())
    }

    fun hentOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> {
        secureLogger.info("Henter oppdragsEnhetsHistorikk for oppdrag: $oppdragsId")
        return oppdragsInfoRepository.hentOppdragsEnhetsHistorikk(oppdragsId.toInt())
    }

    fun hentOppdragsStatusHistorikk(oppdragsId: String): List<OppdragStatus> {
        secureLogger.info("Henter oppdragsStatusHistorikk for oppdrag: $oppdragsId")
        return oppdragsInfoRepository.hentOppdragsStatusHistorikk(oppdragsId.toInt())
    }

    fun hentOppdragsLinjeStatuser(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeStatus> {
        secureLogger.info("Henter oppdragslinjstatuser for oppdrag : $oppdragsId, linje : $linjeId")
        return oppdragsInfoRepository.hentOppdragsLinjeStatuser(oppdragsId.toInt(), linjeId.toInt())
    }

    fun hentOppdragsLinjeAttestanter(
        oppdragsId: String,
        linjeId: String,
    ): List<Attestant> {
        secureLogger.info("Henter oppdragslinjstatuser for oppdrag : $oppdragsId, linje : $linjeId")
        return oppdragsInfoRepository.hentOppdragsLinjeAttestanter(oppdragsId.toInt(), linjeId.toInt())
    }

    fun hentOppdragsLinjeDetaljer(
        oppdragsId: String,
        linjeId: String,
    ): OppdragsLinjeDetaljer {
        secureLogger.info("Henter oppdragslinjedetaljer for oppdrag : $oppdragsId, linje : $linjeId")
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
        secureLogger.info("Henter oppdragslinjevaluta for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentValutaerList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeSkyldner(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        secureLogger.info("Henter oppdragslinjeSkyldner for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentSkyldnereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKravhaver(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        secureLogger.info("Henter oppdragslinjeKravhaver for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKravhavereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        secureLogger.info("Henter oppdragslinjeEnheter for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentEnheterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeGrad(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        secureLogger.info("Henter oppdragslinjeGrad for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentGraderList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeTekst(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        secureLogger.info("Henter oppdragslinjeTekst for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentTeksterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKidListe(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        secureLogger.info("Henter oppdragslinjeKidliste for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKidListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeMaksdato(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        secureLogger.info("Henter oppdragslinjeMaksdato for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentMaksdatoerListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeOvrig(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        secureLogger.info("Henter oppdragslinjeOvrig for oppdrag : $oppdragsId, linje : $linjeId")
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

    /*    private suspend fun hentNavnForGjelderId(gjelderId: String): String =
        when {
            gjelderId.toLong() > 80000000000 -> tpService.getLeverandorNavn(gjelderId).navn
            gjelderId.toLong() < 80000000000 ->
                pdlService.getPersonNavn(gjelderId)?.navn?.firstOrNull()
                    ?.run { mellomnavn?.let { "$fornavn $mellomnavn $etternavn" } ?: "$fornavn $etternavn" } ?: ""

            else -> eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        }*/

    private fun hentSaksbehandler(call: ApplicationCall): Saksbehandler {
        return getSaksbehandler(call)
    }
}
