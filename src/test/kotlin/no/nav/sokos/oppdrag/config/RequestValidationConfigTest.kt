package no.nav.sokos.oppdrag.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.requestvalidation.Validator
import kotlinx.coroutines.runBlocking
import no.nav.sokos.oppdrag.common.model.GjelderIdRequest
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class RequestValidationConfigTest : FunSpec({
    test("token bør returnere NAVident") {
        val config =
            RequestValidationConfig().apply {
                requestValidationCommonConfig()
            }

        val validatorsField = RequestValidationConfig::class.declaredMemberProperties.find { it.name == "validators" }
        validatorsField?.isAccessible = true

        val validators = validatorsField?.get(config) as MutableList<Validator>
        val resultat: ValidationResult = runBlocking { validators.first().validate(GjelderIdRequest("")) }
        (resultat as ValidationResult.Invalid).reasons.first() shouldBe "gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer"
    }
})
