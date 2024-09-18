package no.nav.sokos.oppdrag.attestasjon.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.common.util.Util.validGjelderId

fun RequestValidationConfig.requestValidationAttestasjonConfig() {
    validate<OppdragsRequest> { request ->
        when {
            !request.gjelderId.isNullOrEmpty() && !validGjelderId(request.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            !validateSearchParams(request) -> ValidationResult.Invalid("Ugyldig kombinasjon av søkeparametere")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Minimum ett av kriteriene må være utfylt
 * Faggruppe og Ikke attesterte.
 * Fagområde og Ikke attesterte.
 * Gjelder ID
 * Fagsystem ID og fagområde
 */
private fun validateSearchParams(oppdragsRequest: OppdragsRequest): Boolean {
    var valid = false

    oppdragsRequest.kodeFagGruppe?.takeIf { oppdragsRequest.attestert == false }?.let { valid = true }
    oppdragsRequest.kodeFagOmraade?.takeIf { oppdragsRequest.attestert == false }?.let { valid = true }
    oppdragsRequest.gjelderId?.let { valid = true }
    oppdragsRequest.fagSystemId?.let { oppdragsRequest.kodeFagOmraade?.let { valid = true } }

    return valid
}
