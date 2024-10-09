package no.nav.sokos.oppdrag.attestasjon.service

import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZOsResponse
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.NavIdent
import no.nav.sokos.oppdrag.config.SECURE_LOGGER

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
) {
    fun getOppdrag(
        gjelderId: String? = null,
        fagSystemId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
        attestert: Boolean? = null,
        saksbehandler: NavIdent,
    ): List<Oppdrag> {
        if (!gjelderId.isNullOrBlank()) {
            secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
            auditLogger.auditLog(
                AuditLogg(
                    navIdent = saksbehandler.ident,
                    gjelderId = gjelderId,
                    brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
                ),
            )
        }

        val fagomraader =
            when {
                !kodeFagOmraade.isNullOrBlank() -> listOf(kodeFagOmraade)
                !kodeFagGruppe.isNullOrBlank() -> attestasjonRepository.getFagomraaderForFaggruppe(kodeFagGruppe)
                else -> emptyList()
            }

        return attestasjonRepository.getOppdrag(
            attestert,
            fagSystemId,
            gjelderId,
            fagomraader,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
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
            oppdragslinjer
                .zipWithNext { current, next ->
                    if (current.kodeKlasse == next.kodeKlasse) {
                        current.copy(datoVedtakTom = current.datoVedtakTom ?: next.datoVedtakFom.minusDays(1))
                    } else {
                        current
                    }
                }
                .toList() + oppdragslinjer.last()

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
                },
                saksbehandler.ident,
            )

        return oppdragsdetaljer
    }

    suspend fun attestereOppdrag(
        attestasjonRequest: AttestasjonRequest,
        saksbehandler: NavIdent,
    ): ZOsResponse {
        return zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
    }
}
