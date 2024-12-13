package no.nav.sokos.oppdrag.attestasjon.service

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZosResponse
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.util.CacheUtil
import no.nav.sokos.oppdrag.common.util.CacheUtil.getAsync
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import java.time.Duration

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)
const val ENHETSNUMMER_NOS = "8020"
const val ENHETSNUMMER_NOP = "4819"

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
    private val skjermingService: SkjermingService = SkjermingService(),
    private val oppdragCache: AsyncCache<String, List<Oppdrag>> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(60))
            .maximumSize(10_000)
            .buildAsync(),
) {
    suspend fun getOppdrag(
        request: OppdragsRequest,
        navIdent: NavIdent,
    ): List<OppdragDTO> {
        val gjelderId = request.gjelderId
        var verifiedSkjermingForGjelderId = false
        if (!gjelderId.isNullOrBlank()) {
            secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
            auditLogger.auditLog(
                AuditLogg(
                    navIdent = navIdent.ident,
                    gjelderId = gjelderId,
                    brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
                ),
            )
            if ((gjelderId.toLong() in 1_000_000_001..79_999_999_999) && skjermingService.getSkjermingForIdent(gjelderId, navIdent)) {
                throw AttestasjonException("Mangler rettigheter til å se informasjon!")
            }
            verifiedSkjermingForGjelderId = true
        }

        val fagomraader =
            when {
                !request.kodeFagOmraade.isNullOrBlank() -> listOf(request.kodeFagOmraade)
                !request.kodeFagGruppe.isNullOrBlank() -> attestasjonRepository.getFagomraaderForFaggruppe(request.kodeFagGruppe)
                else -> emptyList()
            }

        val oppdragsListe =
            oppdragCache.getAsync("$gjelderId-${fagomraader.joinToString()}-${request.fagSystemId}-${request.attestert}-${navIdent.ident}") {
                attestasjonRepository.getOppdrag(request.attestert, request.fagSystemId, gjelderId, fagomraader, navIdent.ident)
            }

        return oppdragsListe
            .filter { filterEgenAttestertOppdrag(it, request.visEgenAttestertOppdrag ?: false, navIdent) }
            .filter { hasSaksbehandlerReadAccess(it, navIdent) }
            .map { it.toDTO() }
            .let { list ->
                if (verifiedSkjermingForGjelderId) {
                    list.map { it.copy(erSkjermetForSaksbehandler = false) }
                } else {
                    val skjermingMap = skjermingService.getSkjermingForIdentListe(list.map { it.gjelderId }, navIdent)
                    list.map { it.copy(erSkjermetForSaksbehandler = skjermingMap[it.gjelderId] == true) }
                }
            }.map { it.copy(hasWriteAccess = hasSaksbehandlerWriteAccess(it, navIdent)) }
    }

    fun getFagOmraade(): List<FagOmraade> = attestasjonRepository.getFagOmraader()

    fun getOppdragsdetaljer(
        oppdragsId: Int,
        saksbehandler: NavIdent,
    ): OppdragsdetaljerDTO {
        val oppdragslinjer = attestasjonRepository.getOppdragslinjer(oppdragsId)

        if (oppdragslinjer.isEmpty()) {
            return OppdragsdetaljerDTO(emptyList(), saksbehandler.ident)
        }

        val oppdragslinjerMedDatoVedtakTom =
            oppdragslinjer
                .zipWithNext { current, next ->
                    if (current.kodeKlasse == next.kodeKlasse) {
                        current.copy(datoVedtakTom = current.datoVedtakTom ?: next.datoVedtakFom.minus(1, DateTimeUnit.DAY))
                    } else {
                        current
                    }
                }.toList() + oppdragslinjer.last()

        val linjeIder = oppdragslinjer.map { l -> l.linjeId }.toList()

        val kostnadssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BOS")
        val ansvarssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BEH")
        val attestasjoner = attestasjonRepository.getAttestasjonerForLinjer(oppdragsId, linjeIder)

        val oppdragsdetaljer =
            OppdragsdetaljerDTO(
                oppdragslinjerMedDatoVedtakTom
                    .map { linje ->
                        OppdragslinjeDTO(
                            linje,
                            ansvarssteder[linje.linjeId],
                            kostnadssteder[linje.linjeId],
                            attestasjoner[linje.linjeId] ?: emptyList(),
                        )
                    }.sortedBy { oppdragslinjeDTO -> oppdragslinjeDTO.oppdragsLinje.linjeId },
                saksbehandler.ident,
            )

        return oppdragsdetaljer
    }

    suspend fun attestereOppdrag(
        request: AttestasjonRequest,
        saksbehandler: NavIdent,
    ): ZosResponse {
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = request.gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort en oppdatering på oppdragsId: ${request.oppdragsId}, linjeIder: ${request.linjer.joinToString { it.linjeId.toString() }}",
            ),
        )

        if (!saksbehandler.hasWriteAccessAttestasjon()) {
            throw AttestasjonException("Mangler rettigheter til å attestere oppdrag!")
        }
        val response = zosConnectService.attestereOppdrag(request, saksbehandler.ident)
        removeOppdragCache(request.gjelderId, request.fagSystemId, request.kodeFagOmraade)
        return response
    }

    fun removeOppdragCache(
        gjelderId: String? = null,
        fagSystemId: String? = null,
        fagOmraade: String? = null,
    ) {
        oppdragCache.asMap().keys.forEach { key ->
            if (key.contains(gjelderId.orEmpty()) || CacheUtil.isFagSystemIdPartOfCacheKey(key, fagSystemId.orEmpty()) || key.contains(fagOmraade.orEmpty())) {
                oppdragCache.asMap().remove(key)
            }
        }
    }

    private fun hasSaksbehandlerReadAccess(
        oppdrag: Oppdrag,
        saksbehandler: NavIdent,
    ): Boolean =
        when {
            saksbehandler.hasReadAccessLandsdekkende() -> true
            saksbehandler.hasReadAccessNOS() && (ENHETSNUMMER_NOS == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOS == oppdrag.kostnadsSted) -> true
            saksbehandler.hasReadAccessNOP() && (ENHETSNUMMER_NOP == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOP == oppdrag.kostnadsSted) -> true
            else -> false
        }

    private fun hasSaksbehandlerWriteAccess(
        oppdrag: OppdragDTO,
        saksbehandler: NavIdent,
    ): Boolean =
        when {
            saksbehandler.hasWriteAccessLandsdekkende() -> true
            saksbehandler.hasWriteAccessNOS() && (ENHETSNUMMER_NOS == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOS == oppdrag.kostnadsSted) -> true
            saksbehandler.hasWriteAccessNOP() && (ENHETSNUMMER_NOP == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOP == oppdrag.kostnadsSted) -> true
            else -> false
        }

    private fun filterEgenAttestertOppdrag(
        oppdrag: Oppdrag,
        visEgenAttestertOppdrag: Boolean,
        saksbehandler: NavIdent,
    ): Boolean {
        if (!visEgenAttestertOppdrag) {
            return true
        }
        return oppdrag.attestanter.contains(saksbehandler.ident)
    }
}
