package no.nav.sokos.oppdrag.integration.api

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
import io.mockk.coEvery
import io.mockk.mockk
import io.restassured.RestAssured
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.INTEGRATION_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdResponse
import no.nav.sokos.oppdrag.integration.service.IntegrationService
import org.hamcrest.Matchers.equalTo

private const val PORT = 9090

private lateinit var server: NettyApplicationEngine

private val validationFilter = OpenApiValidationFilter("openapi/integration-v1-swagger.yaml")
private val integrationService = mockk<IntegrationService>()

internal class IntegrationApiTest : FunSpec({

    beforeTest {
        server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
    }

    afterTest {
        server.stop(5, 5)
    }

    test("søk navn for gjelderId skal returnere 200 OK") {

        coEvery { integrationService.getNavnForGjelderId(any(), any()) } returns GjelderIdResponse("Test Testesen")

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(GjelderIdRequest(gjelderId = "12345678901"))
                .port(PORT)
                .post("$INTEGRATION_BASE_API_PATH/hentnavn")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getJsonObject<GjelderIdResponse>("navn").shouldBe("Test Testesen")
    }

    test("sok navn med ugyldig gjelderId skal returnere 400 Bad Request") {

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(GjelderIdRequest(gjelderId = "1234567890"))
            .port(PORT)
            .post("$INTEGRATION_BASE_API_PATH/hentnavn")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer"))
    }
})

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            integrationApi(integrationService)
        }
    }
}
