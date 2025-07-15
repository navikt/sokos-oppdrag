package no.nav.sokos.oppdrag.oppdragsinfo.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOP
import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOS
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.dto.WrappedReponseWithErrorDTO
import no.nav.sokos.oppdrag.config.TEAM_LOGS_MARKER
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsEnhetDTO
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsLinjeDetaljerDTO
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragRepository
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsdetaljerRepository
import no.nav.sokos.oppdrag.security.AdGroup

private val logger = KotlinLogging.logger {}

class OppdragsInfoService(
    private val oppdragsInfoRepository: OppdragRepository = OppdragRepository(),
    private val oppdragsdetaljerRepository: OppdragsdetaljerRepository = OppdragsdetaljerRepository(),
    private val skjermingService: SkjermingService = SkjermingService(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    suspend fun getOppdrag(
        gjelderId: String,
        faggruppeKode: String?,
        saksbehandler: NavIdent,
    ): WrappedReponseWithErrorDTO<Oppdrag> {
        logger.info(marker = TEAM_LOGS_MARKER) { "Søker etter oppdrag med gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på OppdragsInfo",
            ),
        )

        if ((gjelderId.toLong() in 1_000_000_001..79_999_999_999) && skjermingService.getSkjermingForIdent(gjelderId, saksbehandler)) {
            return WrappedReponseWithErrorDTO(errorMessage = "Mangler rettigheter til å se informasjon!")
        }

        val oppdrag = oppdragsInfoRepository.getOppdrag(gjelderId, faggruppeKode).filter { hasSaksBehandlerReadAccess(it, saksbehandler) }

        return WrappedReponseWithErrorDTO(data = oppdrag)
    }

    fun getOppdragsLinjer(oppdragsId: Int): List<OppdragsLinje> = oppdragsInfoRepository.getOppdragsLinjer(oppdragsId)

    fun getBehandlendeEnhetForOppdrag(oppdragsId: Int): OppdragsEnhetDTO {
        val enhet = oppdragsInfoRepository.getOppdragsEnhet(oppdragsId = oppdragsId)
        val behandlendeEnhet = oppdragsInfoRepository.getOppdragsEnhet("BEH", oppdragsId).firstOrNull()

        return OppdragsEnhetDTO(enhet.first(), behandlendeEnhet)
    }

    fun getOppdragsOmposteringer(oppdragsId: Int): List<Ompostering> = oppdragsInfoRepository.getOppdragsOmposteringer(oppdragsId)

    fun getOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> = oppdragsInfoRepository.getOppdragsEnhetsHistorikk(oppdragsId.toInt())

    fun getOppdragsStatusHistorikk(oppdragsId: String): List<OppdragsStatus> = oppdragsInfoRepository.getOppdragsStatusHistorikk(oppdragsId.toInt())

    fun getOppdragsLinjeStatuser(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeStatus> = oppdragsInfoRepository.getOppdragsLinjeStatuser(oppdragsId.toInt(), linjeId.toInt())

    fun getOppdragsLinjeAttestanter(
        oppdragsId: String,
        linjeId: String,
    ): List<Attestant> = oppdragsInfoRepository.getOppdragsLinjeAttestanter(oppdragsId.toInt(), linjeId.toInt())

    fun getOppdragsLinjeDetaljer(
        oppdragsId: String,
        linjeId: String,
    ): OppdragsLinjeDetaljerDTO {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        val oppdragslinjer = oppdragsInfoRepository.getOppdragsLinjer(oppdragsId.toInt())
        val eksisterer = oppdragsInfoRepository.existsValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(oppdragsId.toInt(), linjeId.toInt())
        return OppdragsLinjeDetaljerDTO(
            korrigerteLinjeIder = oppdragslinjer.filter { it.linjeId in korrigerteLinjeIder },
            harValutaer = eksisterer["T_VALUTA"] ?: false,
            harSkyldnere = eksisterer["T_SKYLDNER"] ?: false,
            harKravhavere = eksisterer["T_KRAVHAVER"] ?: false,
            harEnheter = eksisterer["T_LINJEENHET"] ?: false,
            harGrader = eksisterer["T_GRAD"] ?: false,
            harTekster = eksisterer["T_TEKST"] ?: false,
            harKidliste = eksisterer["T_KID"] ?: false,
            harMaksdatoer = eksisterer["T_MAKS_DATO"] ?: false,
        )
    }

    fun getOppdragsLinjeValutaer(
        oppdragsId: String,
        linjeId: String,
    ): List<Valuta> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getValutaer(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeSkyldnere(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getSkyldnere(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeKravhavere(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getKravhavere(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getEnheter(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeGrader(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getGrader(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeTekster(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getTekster(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeKid(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getKid(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeMaksDatoer(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getMaksDatoer(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeOvriger(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsdetaljerRepository.getOvriger(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    private fun findKorrigerteLinjeIder(
        oppdragsId: String,
        linjeId: String,
    ): List<Int> {
        val korrigerteLinjer = oppdragsInfoRepository.getKorreksjoner(oppdragsId)
        val korrigerteLinjeIder: MutableList<Int> = ArrayList()
        if (korrigerteLinjer.isNotEmpty()) {
            var linje = linjeId.toInt()
            for (korreksjon in korrigerteLinjer) {
                val korrLinje = korreksjon.linjeId
                if (korrLinje == linje) {
                    korrigerteLinjeIder.add(korrLinje)
                    linje = korreksjon.linjeIdKorr
                }
            }
            korrigerteLinjeIder.add(linje)
        } else {
            korrigerteLinjeIder.add(linjeId.toInt())
        }
        return korrigerteLinjeIder
    }

    private fun hasSaksBehandlerReadAccess(
        oppdrag: Oppdrag,
        saksbehandler: NavIdent,
    ): Boolean =
        when {
            saksbehandler.hasAdGroupAccess(AdGroup.OPPDRAGSINFO_NASJONALT_READ) -> true
            saksbehandler.hasAdGroupAccess(AdGroup.OPPDRAGSINFO_NOS_READ) &&
                (ENHETSNUMMER_NOS == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOS == oppdrag.kostnadssted) -> true

            saksbehandler.hasAdGroupAccess(AdGroup.OPPDRAGSINFO_NOP_READ) &&
                (ENHETSNUMMER_NOP == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOP == oppdrag.kostnadssted) -> true

            else -> false
        }
}
