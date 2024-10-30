package no.nav.sokos.oppdrag.attestasjon.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.coroutines.runBlocking
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.listener.RequestValidationListener

class RequestValidationAttestasjonConfigTest : FunSpec({

    extensions(RequestValidationListener)

    val config =
        RequestValidationConfig().apply {
            requestValidationAttestasjonConfig()
        }

    val validator = RequestValidationListener.getValidators(config).first()

    context("skal ikke gi valideringsfeil") {

        test("gjelderId") {
            val gjelderIdResult = runBlocking { validator.validate(OppdragsRequest("123456789")) }
            gjelderIdResult shouldBe ValidationResult.Valid
        }

        test("faggruppe, ikke attesterte") {
            val faggruppeResult = runBlocking { validator.validate(OppdragsRequest(kodeFagGruppe = "FAGGRUPPE", attestert = false)) }
            faggruppeResult shouldBe ValidationResult.Valid
        }

        test("fagområde, ikke attesterte") {
            val fagomradeResult = runBlocking { validator.validate(OppdragsRequest(kodeFagOmraade = "FAGOMRAADE", attestert = false)) }
            fagomradeResult shouldBe ValidationResult.Valid
        }

        test("fagområde, fagSystemId") {
            val fagomradeFagsystemIdResult = runBlocking { validator.validate(OppdragsRequest(kodeFagOmraade = "FAGOMRAADE", fagSystemId = "FAGSYSTEMID")) }
            fagomradeFagsystemIdResult shouldBe ValidationResult.Valid
        }

        test("alle parametre") {
            val alleParametreResult =
                runBlocking {
                    validator.validate(
                        OppdragsRequest(
                            "123456789",
                            "faggruppe",
                            "kodeFagomraade",
                            "fagSystemId",
                            true,
                        ),
                    )
                }

            alleParametreResult shouldBe ValidationResult.Valid
        }

        test("fagSystemId, uten fagområde, men med gjelderId") {
            val fagSystemIdUtenFagomradeMedGjelderIdResult =
                runBlocking {
                    validator.validate(
                        OppdragsRequest(
                            gjelderId = "123456789",
                            fagSystemId = "fagSystemId",
                        ),
                    )
                }

            fagSystemIdUtenFagomradeMedGjelderIdResult shouldBe ValidationResult.Valid
        }
    }

    context("Skal gi valideringsfeil") {

        test("Ingen søkeparametre") {
            val ingenSokeParameterResult = runBlocking { validator.validate(OppdragsRequest()) }
            (ingenSokeParameterResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }

        test("fagområde, attesterte") {
            val fagomradeAttestertResult = runBlocking { validator.validate(OppdragsRequest(kodeFagOmraade = "FAGOMRAADE", attestert = true)) }
            (fagomradeAttestertResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }

        test("fagområde, både attesterte og ikke attesterte") {
            val fagomradeBadeAttestertOgIkkeAttestertResult = runBlocking { validator.validate(OppdragsRequest(kodeFagOmraade = "FAGOMRAADE")) }
            (fagomradeBadeAttestertOgIkkeAttestertResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }

        test("faggruppe, attesterte") {
            val faggruppeAttestertResult = runBlocking { validator.validate(OppdragsRequest(kodeFagGruppe = "FAGGRUPPE", attestert = true)) }
            (faggruppeAttestertResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }

        test("faggruppe, både attesterte og ikke attesterte") {
            val faggruppeBadeAttestertOgIkkeAttestertResult = runBlocking { validator.validate(OppdragsRequest(kodeFagGruppe = "FAGGRUPPE")) }
            (faggruppeBadeAttestertOgIkkeAttestertResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }

        test("fagSystemId uten fagområde") {
            val fagSystemIdUtenFagomradeResult =
                runBlocking {
                    validator.validate(
                        OppdragsRequest(
                            fagSystemId = "fagSystemId",
                            kodeFagGruppe = "FAGGRUPPE",
                            attestert = null,
                        ),
                    )
                }

            (fagSystemIdUtenFagomradeResult as ValidationResult.Invalid).reasons.first() shouldBe ("Ugyldig kombinasjon av søkeparametere")
        }
    }
})
