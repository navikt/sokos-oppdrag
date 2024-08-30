package no.nav.sokos.oppdrag.integration.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.common.util.Util.validGjelderId
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest

fun RequestValidationConfig.requestValidationIntegrationConfig() {
    validate<GjelderIdRequest> { gjelderIdRequest ->
        when {
            !validGjelderId(gjelderIdRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
