package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjonsdata
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    fun hentOppdragForAttestering(
        gjelderId: String,
        applicationCall: ApplicationCall,
    ): List<Attestasjonsdata> {
        val saksbehandler = getSaksbehandler(applicationCall)

        secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
            ),
        )

        return attestasjonRepository.sok(gjelderId)
    }
}
