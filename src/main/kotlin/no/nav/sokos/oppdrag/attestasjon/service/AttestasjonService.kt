package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
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
    fun getOppdrag(
        gjelderId: String? = null,
        fagsystemId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
        attestert: Boolean? = null,
        applicationCall: ApplicationCall,
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

        if (!validateSearchParams(
                gjelderId = gjelderId,
                kodeFaggruppe = kodeFagGruppe,
                kodeFagomraade = kodeFagOmraade,
                fagsystemId = fagsystemId,
                attestert = attestert,
            )
        ) {
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Ugyldig kombinasjon av søkeparametre"),
            )
        }

        return attestasjonRepository.getOppdrag(
            gjelderId = gjelderId ?: "",
            fagsystemId = fagsystemId ?: "",
            kodeFaggruppe = kodeFagGruppe ?: "",
            kodeFagomraade = kodeFagOmraade ?: "",
            attestert = attestert,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
    }

    fun getOppdragsDetaljer(oppdragsIder: List<Int>): List<OppdragsDetaljer> {
        return attestasjonRepository.getOppdragsDetaljer(oppdragsIder)
    }
}

/**
 * Minimum ett av kriteriene må være utfylt
 * Faggruppe og Ikke attesterte.
 * Fagområde og Ikke attesterte.
 * Gjelder ID
 * Fagsystem ID og fagområde
 */
fun validateSearchParams(
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
