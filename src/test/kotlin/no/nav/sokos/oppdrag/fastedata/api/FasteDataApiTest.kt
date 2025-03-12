package no.nav.sokos.oppdrag.fastedata.api

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

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
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured

import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.FASTEDATA_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.fastedata.bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode
import no.nav.sokos.oppdrag.fastedata.fagomraader
import no.nav.sokos.oppdrag.fastedata.korrigeringsaarsaker
import no.nav.sokos.oppdrag.fastedata.service.FasteDataService
import no.nav.sokos.oppdrag.fastedata.validator.INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE
import no.nav.sokos.oppdrag.fastedata.ventekriterier
import no.nav.sokos.oppdrag.fastedata.ventestatuskoder

private const val PORT = 9090
private const val FAGOMRAADE = "''!"

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/fastedata-v1-swagger.yaml")
private val fasteDataService = mockk<FasteDataService>()

internal class FasteDataApiTest :
    FunSpec({

        beforeTest {
            server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
        }

        afterTest {
            server.stop(5, 5)
        }

        test("hent fagområder returnerer 200 OK") {
            coEvery { fasteDataService.getFagomraader() } returns fagomraader

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Fagomraade>>(response.asString()) shouldBe fagomraader
        }

        test("hent fagområder returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getFagomraader() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("korrigeringsårsaker tilhørende fagområde returnerer 200 OK") {
            coEvery { fasteDataService.getKorrigeringsaarsaker(any()) } returns korrigeringsaarsaker

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/MYSTB/korrigeringsaarsaker")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Korrigeringsaarsak>>(response.asString()) shouldBe korrigeringsaarsaker
        }

        test("korrigeringsårsaker tilhørende fagområde med ugyldig query parameter returnerer 400 Bad Request") {
            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/''!/korrigeringsaarsaker")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.BadRequest.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()).status shouldBe HttpStatusCode.BadRequest.value
            Json.decodeFromString<ApiError>(response.asString()).message shouldBe INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE
        }

        test("hent korrigeringsaarsaker returnerer 500 Internal Server Error") {

            every { fasteDataService.getKorrigeringsaarsaker("MYST") } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/MYST/korrigeringsaarsaker")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader/MYST/korrigeringsaarsaker",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent bilagstyper returnerer 200 OK") {
            coEvery { fasteDataService.getBilagstyper(any()) } returns bilagstype

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/MYST/bilagstyper")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Bilagstype>>(response.asString()) shouldBe bilagstype
        }

        test("hent bilagstyper returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getBilagstyper(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/MYST/bilagstyper")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader/MYST/bilagstyper",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        // TODO: /fagomraader/{kodeFagomraade}/klassekoder: 200 test
        // TODO: /fagomraader/{kodeFagomraade}/klassekoder: 500 test

        test("ventekriterier returnerer 200 OK") {

            coEvery { fasteDataService.getAllVentekriterier() } returns ventekriterier

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/ventekriterier")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val actualVentekriterier: List<Ventekriterier> = Json.decodeFromString(response.asString())

            actualVentekriterier shouldBe ventekriterier
        }

        test("ventekriterier returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getAllVentekriterier() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/ventekriterier")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/ventekriterier",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("ventestatuskoder returnerer 200 OK") {

            coEvery { fasteDataService.getAllVentestatuskoder() } returns ventestatuskoder

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/ventestatuskoder")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val actualVentestatuskoder: List<Ventestatuskode> = Json.decodeFromString(response.asString())

            actualVentestatuskoder shouldBe ventestatuskoder
        }

        test("ventestatuskoder returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getAllVentestatuskoder() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/ventestatuskoder")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/ventestatuskoder",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }
    })

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            fastedataApi(fasteDataService = fasteDataService)
        }
    }
}
