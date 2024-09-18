package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
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

        return attestasjonRepository.getOppdrag(
            gjelderId = gjelderId ?: "",
            fagSystemId = fagSystemId ?: "",
            kodeFaggruppe = kodeFagGruppe ?: "",
            kodeFagomraade = kodeFagOmraade ?: "",
            attestert = attestert,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
    }

    fun getOppdragsDetaljer(oppdragsId: Int): List<OppdragsDetaljer> {
        return attestasjonRepository.getOppdragsDetaljer(oppdragsId)
    }

    suspend fun attestereOppdrag(
        applicationCall: ApplicationCall,
        attestasjonRequest: AttestasjonRequest,
    ): PostOSAttestasjonResponse200 {
        val saksbehandler = getSaksbehandler(applicationCall)
        return zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
    }
}
