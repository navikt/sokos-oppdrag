package no.nav.sokos.oppdrag.attestasjon.service

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

import mu.KotlinLogging

import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZosResponse
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOP
import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOS
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.dto.WrappedReponseWithErrorDTO
import no.nav.sokos.oppdrag.common.util.CacheUtil
import no.nav.sokos.oppdrag.common.valkey.ValkeyCache
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.security.AdGroup

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
    private val skjermingService: SkjermingService = SkjermingService(),
    private val valkeyCache: ValkeyCache = ValkeyCache("attestasjonService"),
) {
    suspend fun getOppdrag(
        request: OppdragsRequest,
        navIdent: NavIdent,
    ): WrappedReponseWithErrorDTO<OppdragDTO> {
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
                return WrappedReponseWithErrorDTO(errorMessage = "Mangler rettigheter til å se informasjon!")
            }
            verifiedSkjermingForGjelderId = true
        }

        val fagomraader =
            when {
                !request.kodeFagOmraade.isNullOrBlank() -> listOf(request.kodeFagOmraade)
                !request.kodeFagGruppe.isNullOrBlank() -> attestasjonRepository.getFagomraaderForFaggruppe(request.kodeFagGruppe)
                else -> emptyList()
            }

        val oppdragsListe = attestasjonRepository.getOppdrag(gjelderId, request.fagSystemId, fagomraader, request.attestertStatus.attestert, request.attestertStatus.filterEgenAttestert)

        val identer = oppdragsListe.map { it.oppdragGjelderId }.distinct()
        if (identer.size > 1000) {
            throw AttestasjonException("Oppgitte søkekriterier gir for stort treff. Vennligst avgrens søket.")
        }

        val statusFilterOppdragList =
            oppdragsListe.takeIf { request.attestertStatus.filterEgenAttestert != null }?.filter { filterEgenAttestertOppdrag(it, request.attestertStatus.filterEgenAttestert!!, navIdent) }
                ?: oppdragsListe

        return WrappedReponseWithErrorDTO(
            data =
                statusFilterOppdragList.filter { hasSaksbehandlerReadAccess(it, navIdent) }.map { it.toDTO() }.let { list ->
                    if (verifiedSkjermingForGjelderId) {
                        list.map { it.copy(erSkjermetForSaksbehandler = false) }
                    } else {
                        val skjermingMap = skjermingService.getSkjermingForIdentListe(identer, navIdent)
                        list.map { it.copy(erSkjermetForSaksbehandler = skjermingMap[it.oppdragGjelderId] == true) }
                    }
                }.map { it.copy(hasWriteAccess = hasSaksbehandlerWriteAccess(it, navIdent)) },
        )
    }

    fun getOppdragsdetaljer(
        oppdragsId: Int,
        saksbehandler: NavIdent,
    ): OppdragsdetaljerDTO {
        val oppdragslinjer = attestasjonRepository.getOppdragslinjer(oppdragsId)

        if (oppdragslinjer.isEmpty()) {
            return OppdragsdetaljerDTO(emptyList(), saksbehandler.ident)
        }

        val oppdragslinjerMedDatoVedtakTom =
            oppdragslinjer.zipWithNext { current, next ->
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
                oppdragslinjerMedDatoVedtakTom.map { linje ->
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

        if (!saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NASJONALT_WRITE, AdGroup.ATTESTASJON_NOS_WRITE, AdGroup.ATTESTASJON_NOP_WRITE)) {
            return ZosResponse(
                errorMessage = "Mangler rettigheter til å attestere oppdrag!",
            )
        }
        val response = zosConnectService.attestereOppdrag(request, saksbehandler.ident)
        removeOppdragCache(request.gjelderId, request.fagSystemId, request.kodeFagOmraade)
        return response
    }

    private suspend fun removeOppdragCache(
        gjelderId: String? = null,
        fagSystemId: String? = null,
        fagOmraade: String? = null,
    ) {
        valkeyCache.getAllKeys().forEach { key ->
            if (key.contains(gjelderId.orEmpty()) || CacheUtil.isFagSystemIdPartOfCacheKey(key, fagSystemId.orEmpty()) || key.contains(fagOmraade.orEmpty())) {
                valkeyCache.delete(key)
            }
        }
    }

    private fun hasSaksbehandlerReadAccess(
        oppdrag: Oppdrag,
        saksbehandler: NavIdent,
    ): Boolean =
        when {
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NASJONALT_READ) -> true
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NOS_READ) && (ENHETSNUMMER_NOS == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOS == oppdrag.kostnadssted) -> true
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NOP_READ) && (ENHETSNUMMER_NOP == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOP == oppdrag.kostnadssted) -> true
            else -> false
        }

    private fun hasSaksbehandlerWriteAccess(
        oppdrag: OppdragDTO,
        saksbehandler: NavIdent,
    ): Boolean =
        when {
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NASJONALT_WRITE) -> true
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NOS_WRITE) &&
                (ENHETSNUMMER_NOS == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOS == oppdrag.kostnadssted) -> true
            saksbehandler.hasAdGroupAccess(AdGroup.ATTESTASJON_NOP_WRITE) &&
                (ENHETSNUMMER_NOP == oppdrag.ansvarssted || oppdrag.ansvarssted == null && ENHETSNUMMER_NOP == oppdrag.kostnadssted) -> true
            else -> false
        }

    private fun filterEgenAttestertOppdrag(
        oppdrag: Oppdrag,
        egenAttestertOppdrag: Boolean?,
        saksbehandler: NavIdent,
    ): Boolean =
        when (egenAttestertOppdrag) {
            true -> oppdrag.attestanter.any { it.value.contains(saksbehandler.ident) }
            false -> oppdrag.attestanter.filter { !it.value.contains(saksbehandler.ident) }.isNotEmpty()
            else -> false
        }
}
