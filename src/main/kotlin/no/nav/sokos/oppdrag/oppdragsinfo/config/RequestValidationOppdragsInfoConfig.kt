package no.nav.sokos.oppdrag.oppdragsinfo.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult

import no.nav.sokos.oppdrag.common.util.GjelderIdValidator.isValidGjelderId
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest

fun RequestValidationConfig.requestValidationOppdragsInfoConfig() {
    validate<OppdragsRequest> { sokOppdragRequest ->
        when {
            !isValidGjelderId(sokOppdragRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
