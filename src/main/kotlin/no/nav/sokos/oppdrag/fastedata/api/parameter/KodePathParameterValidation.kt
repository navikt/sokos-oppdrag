package no.nav.sokos.oppdrag.fastedata.api.parameter

import io.ktor.server.plugins.requestvalidation.RequestValidationException

internal val kodeRegex = Regex("^[0-9a-zA-ZæøåÆØÅ]{2,8}$")

internal fun validateKodePathParameter(
    parameter: Any,
    value: String,
    errorMessage: String,
) {
    if (!value.matches(kodeRegex)) {
        throw RequestValidationException(parameter, listOf(errorMessage))
    }
}
