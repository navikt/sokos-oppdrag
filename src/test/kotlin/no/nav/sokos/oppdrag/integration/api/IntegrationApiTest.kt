package no.nav.sokos.oppdrag.integration.api

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import io.restassured.RestAssured
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.INTEGRATION_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.integration.service.NameResponse
import no.nav.sokos.oppdrag.integration.service.NameService

private const val PORT = 9090

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/integration-v1-swagger.yaml")
private val nameService = mockk<NameService>()

internal class IntegrationApiTest :
    FunSpec({

        beforeTest {
            server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
        }

        afterTest {
            server.stop(5, 5)
        }

        test("søk navn for gjelderId returnerer 200 OK") {

            val nameResponse = NameResponse("Test Testesen")

            coEvery { nameService.getNavn(any(), any()) } returns nameResponse

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(GjelderIdRequest(gjelderId = "12345678901"))
                    .port(PORT)
                    .post("$INTEGRATION_BASE_API_PATH/hentnavn")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<NameResponse>(response.asString()) shouldBe nameResponse
        }

        test("sok navn med ugyldig gjelderId returnerer 400 Bad Request") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(GjelderIdRequest(gjelderId = "1234567"))
                    .port(PORT)
                    .post("$INTEGRATION_BASE_API_PATH/hentnavn")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.BadRequest.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = "gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer",
                    path = "$INTEGRATION_BASE_API_PATH/hentnavn",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("sok navn med dummy token returnerer 500 Internal Server Error") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "dummytoken")
                    .body(GjelderIdRequest(gjelderId = "123456789"))
                    .port(PORT)
                    .post("$INTEGRATION_BASE_API_PATH/hentnavn")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.InternalServerError.value,
                    error = HttpStatusCode.InternalServerError.description,
                    message = "The token was expected to have 3 parts, but got 0.",
                    path = "$INTEGRATION_BASE_API_PATH/hentnavn",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
        }
    })

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            integrationApi(nameService)
        }
    }
}
