package no.nav.sokos.oppdrag.attestasjon.service

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
    private val ENHETSNUMMER_NOS = "8020"
    private val ENHETSNUMMER_NOP = "4819"

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
    private val skjermingService: SkjermingService = SkjermingService(),
) {
    private val oppdragCache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(60))
            .maximumSize(10_000)
            .buildAsync<String, List<Oppdrag>>()

    suspend fun getOppdrag(
        gjelderId: String? = null,
        fagSystemId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
        attestert: Boolean? = null,
        page: Int,
        rows: Int,
        saksbehandler: NavIdent,
    ): Pair<List<Oppdrag>, Int> =
        coroutineScope {
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

            val oppdragListe = oppdragListeDeferred.await().filter { harEnhetstilgang(it, saksbehandler) }
            val totalCount = oppdragListe.size

            val skjermingMap = skjermingService.getSkjermingForIdentListe(oppdragListe.map { it.gjelderId }, saksbehandler)
            val data = oppdragListe
                .subList((page - 1) * rows, Math.min(page * rows, totalCount))
                .map { it.copy(erSkjermetForSaksbehandler = skjermingMap[it.gjelderId] == true) }
            Pair(data, if (data.size > totalCount) data.size else totalCount)
        }

    private fun harEnhetstilgang(oppdrag:Oppdrag, saksbehandler: NavIdent): Boolean {
                return when {
                    saksbehandler.harTilgangTilLandsdekkende() -> true
                    saksbehandler.harTilgangTilNos() && (ENHETSNUMMER_NOS == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOS == oppdrag.kostnadsSted) -> true
                    saksbehandler.harTilgangTilNop() && (ENHETSNUMMER_NOP == oppdrag.ansvarsSted || oppdrag.ansvarsSted == null && ENHETSNUMMER_NOP == oppdrag.kostnadsSted) -> true
                    else -> false
                }
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
