package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEgenskaper
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsEnhetDTO
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsLinjeDetaljerDTO
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class OppdragsInfoService(
    private val oppdragsInfoRepository: OppdragsInfoRepository = OppdragsInfoRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    fun hentOppdragsEgenskaper(
        gjelderId: String,
        faggruppeKode: String?,
        applicationCall: ApplicationCall,
    ): List<OppdragsEgenskaper> {
        val navIdent = getSaksbehandler(applicationCall)

        secureLogger.info { "Søker etter oppdrag med gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = navIdent.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på OppdragsInfo",
            ),
        )

        val oppdragId = oppdragsInfoRepository.hentOppdragId(gjelderId)

        return oppdragId?.let {
            return oppdragsInfoRepository.hentOppdragsEgenskaperList(gjelderId, faggruppeKode)
        } ?: run {
            secureLogger.info { "Fant ingen oppdrag for gjelderId: $gjelderId" }
            return emptyList()
        }
    }

    fun hentFagGrupper(): List<FagGruppe> {
        logger.info { "Henter faggrupper" }
        return oppdragsInfoRepository.hentFagGrupper()
    }

    fun hentOppdragsLinjer(
        gjelderId: String,
        oppdragsId: Int,
    ): List<OppdragsLinje> {
        logger.info { "Henter oppdragslinjer med oppdragsId: $oppdragsId" }

        val oppdragTilknyttetBruker = oppdragsInfoRepository.erOppdragTilknyttetBruker(gjelderId, oppdragsId)

        if (!oppdragTilknyttetBruker) {
            logger.error { "Oppdraget med id: $oppdragsId er ikke knyttet til bruker" }
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Oppdraget er ikke knyttet til bruker"),
            )
        }

        return oppdragsInfoRepository.hentOppdragsLinjer(oppdragsId)
    }

    fun hentBehandlendeEnhetForOppdrag(oppdragsId: Int): OppdragsEnhetDTO {
        logger.info { "Henter behandlende enhet for oppdrag med oppdragsId: $oppdragsId" }
        val enhet = oppdragsInfoRepository.hentOppdragsEnhet(oppdragsId = oppdragsId).first()
        val behandlendeEnhet = oppdragsInfoRepository.hentOppdragsEnhet("BEH", oppdragsId).firstOrNull()

        return OppdragsEnhetDTO(enhet, behandlendeEnhet)
    }

    fun hentOppdragsOmposteringer(oppdragsId: Int): List<Ompostering> {
        logger.info { "Henter omposteringer med oppdragsId: $oppdragsId" }

        return oppdragsInfoRepository.hentOppdragsOmposteringer(oppdragsId)
    }

    fun hentOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> {
        logger.info { "Henter enhetshistorikk for oppdragId: $oppdragsId" }
        return oppdragsInfoRepository.hentOppdragsEnhetsHistorikk(oppdragsId.toInt())
    }

    fun hentOppdragsStatusHistorikk(oppdragsId: String): List<OppdragsStatus> {
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
    ): OppdragsLinjeDetaljerDTO {
        logger.info { "Henter detaljer for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        val oppdragslinjer = oppdragsInfoRepository.hentOppdragsLinjer(oppdragsId.toInt())
        val eksisterer = oppdragsInfoRepository.eksistererValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(oppdragsId.toInt(), linjeId.toInt())
        return OppdragsLinjeDetaljerDTO(
            korrigerteLinjeIder = oppdragslinjer.filter { it.linjeId in korrigerteLinjeIder },
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

    fun hentOppdragsLinjeValutaer(
        oppdragsId: String,
        linjeId: String,
    ): List<Valuta> {
        logger.info { "Henter valuta for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentValutaerList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeSkyldnere(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        logger.info { "Henter skyldner for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentSkyldnereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKravhavere(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        logger.info { "Henter kravhaver for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKravhavereList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        logger.info { "Henter enheter liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentEnheterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeGrader(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        logger.info { "Henter grad for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentGraderList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeTekster(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        logger.info { "Henter tekst liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentTeksterList(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeKid(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        logger.info { "Henter kid for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentKidListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeMaksdatoer(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        logger.info { "Henter maksdato for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentMaksdatoerListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun hentOppdragsLinjeOvriger(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        logger.info { "Henter øvrig for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = finnKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.hentOvrigListe(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    private fun finnKorrigerteLinjeIder(
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
