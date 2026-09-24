package no.nav.sokos.oppdrag.fastedata.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult

import no.nav.sokos.oppdrag.fastedata.api.model.KodeFagOmraadeRequest
import no.nav.sokos.oppdrag.listener.RequestValidationListener

class RequestValidationFasteDataConfigTest :
    FunSpec({

        extensions(RequestValidationListener)

        val config =
            RequestValidationConfig().apply {
                requestValidationFasteDataConfig()
            }

        val validator = RequestValidationListener.getValidators(config).first()

        context("skal ikke gi valideringsfeil") {

            test("når kodeFagOmraade har gyldig format") {
                validator.validate(KodeFagOmraadeRequest("MYSTB")) shouldBe ValidationResult.Valid
            }
        }

        context("skal gi valideringsfeil") {

            test("når kodeFagOmraade er for kort") {
                val result = validator.validate(KodeFagOmraadeRequest("M"))

                (result as ValidationResult.Invalid).reasons shouldBe listOf(INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE)
            }

            test("når kodeFagOmraade inneholder ugyldige tegn") {
                val result = validator.validate(KodeFagOmraadeRequest("MYST!"))

                (result as ValidationResult.Invalid).reasons shouldBe listOf(INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE)
            }
        }
    })
