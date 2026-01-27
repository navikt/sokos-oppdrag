package no.nav.sokos.oppdrag.fastedata.api

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.json.Json

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter
import io.kotest.common.KotestInternal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType
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

import no.nav.sokos.oppdrag.attestasjon.FASTEDATA_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.fastedata.alleKlassekoder
import no.nav.sokos.oppdrag.fastedata.bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Faggruppe
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Kjoreplan
import no.nav.sokos.oppdrag.fastedata.domain.Klassekode
import no.nav.sokos.oppdrag.fastedata.domain.Klassekoder
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.RedusertSkatt
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode
import no.nav.sokos.oppdrag.fastedata.faggrupper
import no.nav.sokos.oppdrag.fastedata.fagomraader
import no.nav.sokos.oppdrag.fastedata.klassekoder
import no.nav.sokos.oppdrag.fastedata.korrigeringsaarsaker
import no.nav.sokos.oppdrag.fastedata.service.FasteDataService
import no.nav.sokos.oppdrag.fastedata.validator.INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE
import no.nav.sokos.oppdrag.fastedata.ventekriterier
import no.nav.sokos.oppdrag.fastedata.ventestatuskoder

private const val PORT = 9090
private const val KODE_FAGOMRAADE_MYST = "MYST"
private const val KODE_FAGOMRAADE_MYSTB = "MYSTB"
private const val KODE_FAGGRUPPE_BA = "BA"

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/fastedata-v1-swagger.yaml")
private val fasteDataService = mockk<FasteDataService>()

@OptIn(KotestInternal::class, ExperimentalTime::class)
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYSTB/korrigeringsaarsaker")
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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

            every { fasteDataService.getKorrigeringsaarsaker(KODE_FAGOMRAADE_MYST) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/korrigeringsaarsaker")
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
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/korrigeringsaarsaker",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent bilagstyper returnerer 200 OK") {
            coEvery { fasteDataService.getBilagstyper(any()) } returns bilagstype

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/bilagstyper")
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/bilagstyper")
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
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/bilagstyper",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent klassekode returnerer 200 OK") {
            coEvery { fasteDataService.getKlassekoder(any()) } returns klassekoder

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/klassekoder")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Klassekode>>(response.asString()) shouldBe klassekoder
        }

        test("hent klassekoder returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getKlassekoder(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/klassekoder")
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
                    path = "$FASTEDATA_BASE_API_PATH/fagomraader/$KODE_FAGOMRAADE_MYST/klassekoder",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("ventekriterier returnerer 200 OK") {

            coEvery { fasteDataService.getAllVentekriterier() } returns ventekriterier

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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

        test("klassekoder returnerer 200 OK") {
            coEvery { fasteDataService.getAllKlassekoder() } returns alleKlassekoder

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/klassekoder")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val actualKlassekoder: List<Klassekoder> = Json.decodeFromString(response.asString())

            actualKlassekoder shouldBe alleKlassekoder
        }

        test("klassekoder returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getAllKlassekoder() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/klassekoder")
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
                    path = "$FASTEDATA_BASE_API_PATH/klassekoder",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("faggrupper returnerer 200 OK") {
            coEvery { fasteDataService.getFaggrupper() } returns faggrupper

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val actualFaggrupper: List<Faggruppe> = Json.decodeFromString(response.asString())

            actualFaggrupper shouldBe faggrupper
        }
        test("faggrupper returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getFaggrupper() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper")
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
                    path = "$FASTEDATA_BASE_API_PATH/faggrupper",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }
        test("redusert skatt returnerer 200 OK") {
            val redusertSkatt =
                listOf(
                    RedusertSkatt(
                        kodeFaggruppe = KODE_FAGGRUPPE_BA,
                        datoFom = "2024-01-01",
                        datoTom = "2024-03-31",
                        prosent = 25,
                    ),
                )
            coEvery { fasteDataService.getRedusertSkatt(KODE_FAGGRUPPE_BA) } returns redusertSkatt

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/redusertSkatt")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val actual: List<RedusertSkatt> = Json.decodeFromString(response.asString())
            actual shouldBe redusertSkatt
        }

        test("redusert skatt returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getRedusertSkatt(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/redusertSkatt")
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
                    path = "$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/redusertSkatt",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("kjoreplan returnerer 200 OK") {
            val kjoreplanList =
                listOf(
                    Kjoreplan(
                        kodeFaggruppe = KODE_FAGGRUPPE_BA,
                        datoKjores = "1989-01-08",
                        status = "AVSL",
                        datoForfall = "1989-01-28",
                        datoOverfores = null,
                        datoBeregnFom = "1999-12-13",
                        datoBeregnTom = "1999-12-31",
                    ),
                )
            coEvery { fasteDataService.getKjoreplan(KODE_FAGGRUPPE_BA) } returns kjoreplanList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/kjoreplan")
                    .then()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()

            val actual: List<Kjoreplan> = Json.decodeFromString(response.asString())
            actual shouldBe kjoreplanList
        }

        test("kjoreplan returnerer 500 Internal Server Error") {
            coEvery { fasteDataService.getKjoreplan(KODE_FAGGRUPPE_BA) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/kjoreplan")
                    .then()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "En feil",
                    path = "$FASTEDATA_BASE_API_PATH/faggrupper/$KODE_FAGGRUPPE_BA/kjoreplan",
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
