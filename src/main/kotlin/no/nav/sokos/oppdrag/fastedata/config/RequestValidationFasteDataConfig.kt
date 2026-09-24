package no.nav.sokos.oppdrag.fastedata.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult

import no.nav.sokos.oppdrag.fastedata.api.model.KodeFagOmraadeRequest

const val INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE = "kodeFagomraade må være mellom 2 og 8 tegn og kan kun inneholde bokstaver og tall"

private val kodeFagOmraadeRegex = Regex("^[0-9a-zA-ZæøåÆØÅ]{2,8}$")

fun RequestValidationConfig.requestValidationFasteDataConfig() {
    validate<KodeFagOmraadeRequest> {
        it.validationResult()
    }
}

fun KodeFagOmraadeRequest.validateKodeFagOmraade() {
    when (val result = validationResult()) {
        is ValidationResult.Invalid -> throw RequestValidationException(this, result.reasons)
        ValidationResult.Valid -> Unit
    }
}

private fun KodeFagOmraadeRequest.validationResult(): ValidationResult =
    if (kodeFagOmraade.matches(kodeFagOmraadeRegex)) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE)
    }
