package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
) {
    fun getOppdrag(
        applicationCall: ApplicationCall,
        attestert: Boolean? = null,
        fagSystemId: String? = null,
        gjelderId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
    ): List<Oppdrag> {
        if (!gjelderId.isNullOrBlank()) {
            val saksbehandler = getSaksbehandler(applicationCall)
            secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
            auditLogger.auditLog(
                AuditLogg(
                    navIdent = saksbehandler.ident,
                    gjelderId = gjelderId,
                    brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
                ),
            )
        }

        // hvis faggruppe er oppgitt kan vi søke på alle de fagområdene som er innunder den faggruppen
        var fagomraader = kodeFagGruppe?.let { attestasjonRepository.getFagomraaderForFaggruppe(it) }

        // hvis fagområde er oppgitt er det bare det ene vi skal søke på
        kodeFagOmraade?. let { if (kodeFagOmraade.isNotBlank()) fagomraader = listOf(it) }

        return attestasjonRepository.getOppdrag(
            attestert = attestert,
            fagSystemId = fagSystemId,
            gjelderId = gjelderId,
            kodeFagOmraader = fagomraader,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
    }

    fun getOppdragsdetaljer(
        applicationCall: ApplicationCall,
        oppdragsId: Int,
    ): List<OppdragsdetaljerDTO> {
        val oppdragslinjer: List<Oppdragslinje> = attestasjonRepository.getOppdragslinjer(oppdragsId)

        val linjerMedDatoVedtakTom: List<Oppdragslinje> =
            oppdragslinjer
                .groupBy { l -> l.kodeKlasse }
                .values.flatMap { l ->
                    l.zipWithNext()
                        .map { (curr, next) ->
                            curr.copy(
                                datoVedtakTom = curr.datoVedtakTom ?: next.datoVedtakFom.minusDays(1),
                            )
                        }
                        .toList() + l.last()
                }

        val linjeIder = oppdragslinjer.map { l -> l.linjeId }.toList()

        val kostnadssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BOS")
        val ansvarssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BEH")
        val attestasjoner: Map<Int, List<Attestasjon>> = attestasjonRepository.getAttestasjonerForLinjer(oppdragsId, linjeIder)

        val oppdragsdetaljer =
            OppdragsdetaljerDTO(
                saksbehandlerIdent = getSaksbehandler(applicationCall).ident,
                linjer =
                    linjerMedDatoVedtakTom.map { l ->
                        OppdragslinjeDTO(
                            oppdragsLinje = l,
                            ansvarsStedForOppdragsLinje = ansvarssteder[l.linjeId],
                            kostnadsStedForOppdragsLinje = kostnadssteder[l.linjeId],
                            attestasjoner = attestasjoner[l.linjeId] ?: emptyList(),
                        )
                    },
            )
        return listOf(oppdragsdetaljer)
    }

    suspend fun attestereOppdrag(
        applicationCall: ApplicationCall,
        attestasjonRequest: AttestasjonRequest,
    ): PostOSAttestasjonResponse200 {
        val saksbehandler = getSaksbehandler(applicationCall)
        return zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
    }
}
