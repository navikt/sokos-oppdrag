package no.nav.sokos.oppdrag.oppdragsinfo.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult

import no.nav.sokos.oppdrag.listener.RequestValidationListener
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest

class RequestValidationOppdragsInfoConfigTest :
    FunSpec({
        extensions(RequestValidationListener)

        val config =
            RequestValidationConfig().apply {
                requestValidationOppdragsInfoConfig()
            }

        val validator = RequestValidationListener.getValidators(config).first()

        context("skal ikke gi valideringsfeil") {

            test("når gjelderId er 9 siffer") {
                val gjelderIdResult = validator.validate(OppdragsRequest("123456789"))
                gjelderIdResult shouldBe ValidationResult.Valid
            }

            test("når gjelderId er 11 siffer") {
                val gjelderIdResult = validator.validate(OppdragsRequest("12345678912"))
                gjelderIdResult shouldBe ValidationResult.Valid
            }
        }

        context("skal gi valideringsfeil") {

            test("når gjelderId er 8 siffer") {
                val gjelderIdResult = validator.validate(OppdragsRequest("12345678"))
                (gjelderIdResult as ValidationResult.Invalid).reasons.first() shouldBe ("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            }

            test("når gjelderId er 12 siffer") {
                val gjelderIdResult = validator.validate(OppdragsRequest("123456789123"))
                (gjelderIdResult as ValidationResult.Invalid).reasons.first() shouldBe ("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer")
            }
        }
    })
