package no.nav.sokos.oppdrag.oppdragsinfo.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.common.util.Util.validGjelderId
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsInfoRequest

fun RequestValidationConfig.requestValidationOppdragsInfoConfig() {
    validate<OppdragsInfoRequest> { sokOppdragRequest ->
        when {
            !validGjelderId(sokOppdragRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
