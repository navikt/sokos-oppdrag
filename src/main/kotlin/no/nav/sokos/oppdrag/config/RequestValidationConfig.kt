package no.nav.sokos.oppdrag.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.common.model.GjelderIdRequestBody
import no.nav.sokos.oppdrag.common.util.Util.validGjelderId

fun RequestValidationConfig.requestValidationCommonConfig() {
    validate<GjelderIdRequestBody> { gjelderIdRequest ->
        when {
            !validGjelderId(gjelderIdRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
