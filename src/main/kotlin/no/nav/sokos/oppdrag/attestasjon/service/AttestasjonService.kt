package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.domain.AttestasjonTreff
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjonsdetaljer
import no.nav.sokos.oppdrag.attestasjon.domain.Fagomraade
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

        if (!validerSok(
                gjelderId = gjelderId,
                kodeFaggruppe = kodeFaggruppe,
                kodeFagomraade = kodeFagomraade,
                fagsystemId = fagsystemId,
                attestert = attestert,
            )
        ) {
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Ugyldig kombinasjon av søkeparametre"),
            )
        }

        return attestasjonRepository.sok(
            gjelderId = gjelderId ?: "",
            fagsystemId = fagsystemId ?: "",
            kodeFaggruppe = kodeFaggruppe ?: "",
            kodeFagomraade = kodeFagomraade ?: "",
            attestert = attestert,
        )
    }

    fun hentOppdragslinjerForAttestering(oppdragsId: Int): List<Attestasjonsdetaljer> {
        return attestasjonRepository.hentOppdragslinjer(oppdragsId)
    }

    fun hentFagomraader(): List<Fagomraade> {
        return attestasjonRepository.hentFagomraader()
    }

    fun hentListeMedOppdragslinjerForAttestering(oppdragsIder: List<Int>): List<Attestasjonsdetaljer> {
        return attestasjonRepository.hentOppdragslinjerForFlereOppdragsId(oppdragsIder)
    }
}

/**
 * Minimum ett av kriteriene må være utfylt
 * Faggruppe og Ikke attesterte.
 * Fagområde og Ikke attesterte.
 * Gjelder ID
 * Fagsystem ID og fagområde
 */
fun validerSok(
    kodeFaggruppe: String?,
    kodeFagomraade: String?,
    gjelderId: String?,
    fagsystemId: String?,
    attestert: Boolean?,
): Boolean {
    var gyldig = false

    kodeFaggruppe?.takeIf { attestert == false }?.let { gyldig = true }
    kodeFagomraade?.takeIf { attestert == false }?.let { gyldig = true }
    gjelderId?.let { gyldig = true }
    fagsystemId?.let { kodeFagomraade?.let { gyldig = true } }

    return gyldig
}
