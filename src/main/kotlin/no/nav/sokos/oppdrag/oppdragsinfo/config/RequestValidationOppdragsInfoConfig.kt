package no.nav.sokos.oppdrag.oppdragsinfo.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.common.model.SokOppdragRequestBody
import no.nav.sokos.oppdrag.common.util.Util.validGjelderId

fun RequestValidationConfig.requestValidationOppdragsInfoConfig() {
    validate<SokOppdragRequestBody> { sokOppdragRequest ->
        when {
            !validGjelderId(sokOppdragRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
