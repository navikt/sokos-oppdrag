package no.nav.sokos.oppdrag.oppdragsinfo.config

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.SokOppdragRequest
import no.nav.sokos.oppdrag.oppdragsinfo.util.Util.validGjelderId

fun RequestValidationConfig.oppdragsInfoRequestValidationConfig() {
    validate<SokOppdragRequest> { sokOppdragRequest ->
        when {
            !validGjelderId(sokOppdragRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }

    validate<GjelderIdRequest> { gjelderIdRequest ->
        when {
            !validGjelderId(gjelderIdRequest.gjelderId) -> ValidationResult.Invalid("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            else -> ValidationResult.Valid
        }
    }
}
