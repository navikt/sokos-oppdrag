package no.nav.sokos.oppdrag.oppdragsinfo.service

import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.NavIdent
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
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class OppdragsInfoService(
    private val oppdragsInfoRepository: OppdragsInfoRepository = OppdragsInfoRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    fun getOppdrag(
        gjelderId: String,
        faggruppeKode: String?,
        saksbehandler: NavIdent,
    ): List<Oppdrag> {
        secureLogger.info { "Søker etter oppdrag med gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på OppdragsInfo",
            ),
        )

        val oppdragId = oppdragsInfoRepository.getOppdragId(gjelderId)

        return oppdragId?.let {
            return oppdragsInfoRepository.getOppdrag(gjelderId, faggruppeKode)
        } ?: run {
            secureLogger.info { "Fant ingen oppdrag for gjelderId: $gjelderId" }
            return emptyList()
        }
    }

    fun getFagGrupper(): List<FagGruppe> {
        logger.info { "Henter faggrupper" }
        return oppdragsInfoRepository.getFagGrupper()
    }

    fun getOppdragsLinjer(oppdragsId: Int): List<OppdragsLinje> {
        logger.info { "Henter oppdragslinjer med oppdragsId: $oppdragsId" }

        return oppdragsInfoRepository.getOppdragsLinjer(oppdragsId)
    }

    fun getBehandlendeEnhetForOppdrag(oppdragsId: Int): OppdragsEnhetDTO {
        logger.info { "Henter behandlende enhet for oppdrag med oppdragsId: $oppdragsId" }
        val enhet = oppdragsInfoRepository.getOppdragsEnhet(oppdragsId = oppdragsId).first()
        val behandlendeEnhet = oppdragsInfoRepository.getOppdragsEnhet("BEH", oppdragsId).firstOrNull()

        return OppdragsEnhetDTO(enhet, behandlendeEnhet)
    }

    fun getOppdragsOmposteringer(oppdragsId: Int): List<Ompostering> {
        logger.info { "Henter omposteringer med oppdragsId: $oppdragsId" }

        return oppdragsInfoRepository.getOppdragsOmposteringer(oppdragsId)
    }

    fun getOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> {
        logger.info { "Henter enhetshistorikk for oppdragId: $oppdragsId" }
        return oppdragsInfoRepository.getOppdragsEnhetsHistorikk(oppdragsId.toInt())
    }

    fun getOppdragsStatusHistorikk(oppdragsId: String): List<OppdragsStatus> {
        logger.info { "Henter statushistorikk for oppdragId: $oppdragsId" }
        return oppdragsInfoRepository.getOppdragsStatusHistorikk(oppdragsId.toInt())
    }

    fun getOppdragsLinjeStatuser(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeStatus> {
        logger.info { "Henter linjstatus for oppdrag: $oppdragsId, linje: $linjeId" }
        return oppdragsInfoRepository.getOppdragsLinjeStatuser(oppdragsId.toInt(), linjeId.toInt())
    }

    fun getOppdragsLinjeAttestanter(
        oppdragsId: String,
        linjeId: String,
    ): List<Attestant> {
        logger.info { "Henter attestant for oppdrag: $oppdragsId, linje : $linjeId" }
        return oppdragsInfoRepository.getOppdragsLinjeAttestanter(oppdragsId.toInt(), linjeId.toInt())
    }

    fun getOppdragsLinjeDetaljer(
        oppdragsId: String,
        linjeId: String,
    ): OppdragsLinjeDetaljerDTO {
        logger.info { "Henter detaljer for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        val oppdragslinjer = oppdragsInfoRepository.getOppdragsLinjer(oppdragsId.toInt())
        val eksisterer = oppdragsInfoRepository.existsValutaSkyldnerKravhaverLinjeenhetGradTekstKidMaksDato(oppdragsId.toInt(), linjeId.toInt())
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

    fun getOppdragsLinjeValutaer(
        oppdragsId: String,
        linjeId: String,
    ): List<Valuta> {
        logger.info { "Henter valuta for oppdrag: $oppdragsId, linje : $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getValutaer(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeSkyldnere(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        logger.info { "Henter skyldner for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getSkyldnere(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeKravhavere(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        logger.info { "Henter kravhaver for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getKravhavere(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        logger.info { "Henter enheter liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getEnheter(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeGrader(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        logger.info { "Henter grad for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getGrader(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeTekster(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        logger.info { "Henter tekst liste for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getTekster(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeKid(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        logger.info { "Henter kid for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getKid(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeMaksDatoer(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        logger.info { "Henter maksdato for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getMaksDatoer(oppdragsId.toInt(), korrigerteLinjeIder)
    }

    fun getOppdragsLinjeOvriger(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        logger.info { "Henter øvrig for oppdrag: $oppdragsId, linje: $linjeId" }
        val korrigerteLinjeIder: List<Int> = findKorrigerteLinjeIder(oppdragsId, linjeId)
        return oppdragsInfoRepository.getOvriger(oppdragsId.toInt(), korrigerteLinjeIder)
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
