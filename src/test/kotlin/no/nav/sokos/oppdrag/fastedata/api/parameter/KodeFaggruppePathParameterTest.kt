package no.nav.sokos.oppdrag.fastedata.api.parameter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationException

class KodeFaggruppePathParameterTest :
    FunSpec({

        test("skal godta kodeFaggruppe med gyldig format") {
            val parameter = KodeFaggruppePathParameter(kodeFaggruppe = "BA")

            parameter.kodeFaggruppe shouldBe "BA"
            parameter.validateKodeFaggruppe()
        }

        test("skal avvise kodeFaggruppe med under to tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFaggruppePathParameter(kodeFaggruppe = "B").validateKodeFaggruppe()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGGRUPPE_PATH_PARAMETER_MESSAGE)
        }

        test("skal avvise kodeFaggruppe med mer enn åtte tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFaggruppePathParameter(kodeFaggruppe = "FAGGRUPPE").validateKodeFaggruppe()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGGRUPPE_PATH_PARAMETER_MESSAGE)
        }

        test("skal avvise kodeFaggruppe med ugyldig tegn") {
            val exception =
                shouldThrow<RequestValidationException> {
                    KodeFaggruppePathParameter(kodeFaggruppe = "BA!").validateKodeFaggruppe()
                }

            exception.reasons shouldBe listOf(INVALID_KODE_FAGGRUPPE_PATH_PARAMETER_MESSAGE)
        }
    })
