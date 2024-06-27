package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.domain.AttestasjonTreff
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjonsdetaljer
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
        gjelderId: String? = null,
        fagsystemId: String? = null,
        kodeFaggruppe: String? = null,
        kodeFagomraade: String? = null,
        attestert: Boolean? = null,
        applicationCall: ApplicationCall,
    ): List<AttestasjonTreff> {
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

        return attestasjonRepository.sok(
            gjelderId = gjelderId ?: "",
            fagsystemId = fagsystemId ?: "",
            kodeFaggruppe = kodeFaggruppe ?: "",
            kodeFagomraade = kodeFagomraade ?: "",
            attestert =
                if (attestert == null) {
                    "%"
                } else if (attestert) {
                    "J"
                } else {
                    "N"
                },
        )
    }

    fun hentOppdragslinjerForAttestering(oppdragsId: Int): List<Attestasjonsdetaljer> {
        return attestasjonRepository.hentOppdragslinjer(oppdragsId)
    }
}
