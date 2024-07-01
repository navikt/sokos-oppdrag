package no.nav.sokos.oppdrag.attestasjon.api

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.routing
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.ATTESTASJON_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.domain.AttestasjonTreff
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.common.model.GjelderIdRequestBody
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig

private const val PORT = 9090

private lateinit var server: NettyApplicationEngine

private val validationFilter = OpenApiValidationFilter("openapi/attestasjon-v1-swagger.yaml")
private val attestasjonService = mockk<AttestasjonService>()

internal class AttestasjonApiTest : FunSpec({

    beforeTest {
        server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
    }

    afterTest {
        server.stop(5, 5)
    }

    test("søk etter gjelderId på gjeldersok endepunktet skal returnere 200 OK") {
        val attestasjontreff =
            AttestasjonTreff(
                gjelderId = "12345678901",
                navnFaggruppe = "navnFaggruppe",
                navnFagomraade = "navnFagomraade",
                oppdragsId = 987654,
                fagsystemId = "123456789",
            )

        val attestasjonTreffliste = listOf(attestasjontreff)

        every { attestasjonService.hentOppdragForAttestering(any(), any(), any(), any(), any(), any()) } returns attestasjonTreffliste

        val response =
            RestAssured.given().filter(validationFilter).header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(GjelderIdRequestBody("123456789")).port(PORT)
                .post("$ATTESTASJON_BASE_API_PATH/gjeldersok").then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<AttestasjonTreff>("oppdragsId").first().shouldBe(987654)
    }
})

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            attestasjonApi(attestasjonService)
        }
    }
}
