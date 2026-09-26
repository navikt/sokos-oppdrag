package no.nav.sokos.oppdrag.fastedata.api.parameter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationException

class KodeFagOmraadePathParameterTest :
    FunSpec({

        test("skal godta kodeFagOmraade med gyldig format") {
            val parameter = KodeFagOmraadePathParameter(kodeFagOmraade = "MYSTB")

            parameter.kodeFagOmraade shouldBe "MYSTB"
            parameter.validateKodeFagOmraade()
        }

        test("skal avvise kodeFagOmraade med under to tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFagOmraadePathParameter(kodeFagOmraade = "M").validateKodeFagOmraade()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGOMRAADE_PATH_PARAMETER_MESSAGE)
        }

        test("skal avvise kodeFagOmraade med mer enn åtte tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFagOmraadePathParameter(kodeFagOmraade = "MYSTB1234").validateKodeFagOmraade()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGOMRAADE_PATH_PARAMETER_MESSAGE)
        }

        test("skal avvise kodeFagOmraade med ugyldig tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFagOmraadePathParameter(kodeFagOmraade = "MYST!").validateKodeFagOmraade()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGOMRAADE_PATH_PARAMETER_MESSAGE)
        }
    })
