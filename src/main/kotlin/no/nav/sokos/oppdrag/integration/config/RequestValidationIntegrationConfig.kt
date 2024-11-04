package no.nav.sokos.oppdrag.integration.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.common.util.GjelderIdValidator.isValidGjelderId
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest

fun RequestValidationConfig.requestValidationIntegrationConfig() {
    validate<GjelderIdRequest> { gjelderIdRequest ->
        when {
            !isValidGjelderId(gjelderIdRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
