package no.nav.sokos.oppdrag.attestasjon.service

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZOsResponse
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
import no.nav.sokos.oppdrag.common.util.getAsync
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import java.time.Duration

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)
private val logger = KotlinLogging.logger {}
const val ENHETSNUMMER_NOS = "8020"
const val ENHETSNUMMER_NOP = "4819"

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
    private val skjermingService: SkjermingService = SkjermingService(),
    private val oppdragCache: AsyncCache<String, List<Oppdrag>> =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(60))
            .maximumSize(10_000)
            .buildAsync(),
) {
    suspend fun getOppdrag(
        gjelderId: String? = null,
        fagSystemId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
        attestert: Boolean? = null,
        page: Int,
        rows: Int,
        saksbehandler: NavIdent,
    ): Pair<List<OppdragDTO>, Int> =
        coroutineScope {
            var verifiedSkjermingForGjelderId = false
            if (!gjelderId.isNullOrBlank()) {
                secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
                auditLogger.auditLog(
                    AuditLogg(
                        navIdent = saksbehandler.ident,
                        gjelderId = gjelderId,
                        brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
                    ),
                )
                if ((gjelderId.toLong() in 1_000_000_001..79_999_999_999) && skjermingService.getSkjermingForIdent(gjelderId, saksbehandler)) {
                    throw AttestasjonException("Mangler rettigheter til å se informasjon!")
                }
                verifiedSkjermingForGjelderId = true
            }

            val fagomraader =
                when {
                    !kodeFagOmraade.isNullOrBlank() -> listOf(kodeFagOmraade)
                    !kodeFagGruppe.isNullOrBlank() -> attestasjonRepository.getFagomraaderForFaggruppe(kodeFagGruppe)
                    else -> emptyList()
                }

            val oppdragListeDeferred =
                async {
                    oppdragCache.getAsync("$gjelderId-${fagomraader.joinToString()}-$fagSystemId-$attestert") {
                        attestasjonRepository.getOppdrag(attestert, fagSystemId, gjelderId, fagomraader)
                    }
                }

            val temp = oppdragListeDeferred.await()
            val oppdragListe = temp.filter { hasSaksbehandlerReadAccess(it, saksbehandler) }
            val totalCount = oppdragListe.size

            val data =
                oppdragListe
                    .subList((page - 1) * rows, Math.min(page * rows, totalCount))
                    .map { it.toDTO() }
                    .let { list ->
                        if (verifiedSkjermingForGjelderId) {
                            list.map { it.copy(erSkjermetForSaksbehandler = false) }
                        } else {
                            val skjermingMap = skjermingService.getSkjermingForIdentListe(list.map { it.gjelderId }, saksbehandler)
                            list.map { it.copy(erSkjermetForSaksbehandler = skjermingMap[it.gjelderId] == true) }
                        }
                    }
                    .map { it.copy(hasWriteAccess = hasSaksbehandlerWriteAccess(it, saksbehandler)) }
            Pair(data, if (data.size > totalCount) data.size else totalCount)
        }

    private fun hasSaksbehandlerReadAccess(
        oppdrag: Oppdrag,
        saksbehandler: NavIdent,
    ): Boolean {
        logger.info { "Rollene: ${saksbehandler.roller.joinToString()}" }
        logger.info { " saksbehandler.hasReadAccessLandsdekkende(): ${saksbehandler.hasReadAccessLandsdekkende()}" }
        logger.info { " saksbehandler.hasReadAccessNOS(): ${saksbehandler.hasReadAccessNOS()}" }
        logger.info { " saksbehandler.hasReadAccessNOP(): ${saksbehandler.hasReadAccessNOP()}" }

        return when {
            saksbehandler.hasReadAccessLandsdekkende() -> true
            saksbehandler.hasReadAccessNOS() && (ENHETSNUMMER_NOS == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOS == oppdrag.kostnadsSted) -> true
            saksbehandler.hasReadAccessNOP() && (ENHETSNUMMER_NOP == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOP == oppdrag.kostnadsSted) -> true
            else -> true
        }
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
        attestasjonRequest: AttestasjonRequest,
        saksbehandler: NavIdent,
    ): ZOsResponse = zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
}
