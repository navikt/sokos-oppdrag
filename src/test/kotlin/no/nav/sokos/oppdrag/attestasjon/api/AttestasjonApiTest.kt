package no.nav.sokos.oppdrag.attestasjon.api

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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.ATTESTASJON_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZOsResponse
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.attestasjon.utils.GJELDER_ID
import no.nav.sokos.oppdrag.attestasjon.utils.Testdata.oppdragDTOMockdata
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest

private const val PORT = 9090

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/attestasjon-v1-swagger.yaml")
private val attestasjonService = mockk<AttestasjonService>()

internal class AttestasjonApiTest :
    FunSpec({

        beforeTest {
            server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
        }

        afterTest {
            server.stop(5, 5)
        }

        test("søk etter oppdrag med gyldig gjelderId returnerer 200 OK") {
            val oppdragsListe = List(11) { oppdragDTOMockdata }
            coEvery { attestasjonService.getOppdrag(any(), any(), any(), any(), any(), any()) } returns oppdragsListe

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(OppdragsRequest(gjelderId = GJELDER_ID))
                    .port(PORT)
                    .post("$ATTESTASJON_BASE_API_PATH/sok")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val oppdragDTOList = Json.decodeFromString<List<OppdragDTO>>(response.body.asString())
            oppdragDTOList shouldBe oppdragsListe
            oppdragDTOList.size shouldBe oppdragsListe.size
        }

        test("sok etter oppdrag med ugyldig gjelderId returnerer 400 Bad Request") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(OppdragsRequest(gjelderId = "123"))
                    .port(PORT)
                    .post("$ATTESTASJON_BASE_API_PATH/sok")
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
                    path = "$ATTESTASJON_BASE_API_PATH/sok",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("sok etter oppdrag med ugyldige søkeparametere returnerer 400 Bad Request") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(OppdragsRequest(kodeFagGruppe = "BP", attestert = true))
                    .port(PORT)
                    .post("$ATTESTASJON_BASE_API_PATH/sok")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.BadRequest.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = "Ugyldig kombinasjon av søkeparametere",
                    path = "$ATTESTASJON_BASE_API_PATH/sok",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("sok etter oppdrag med gyldig søkeparametere returnerer 200 OK") {

            coEvery { attestasjonService.getOppdrag(any(), any(), any(), any(), any(), any()) } returns emptyList()

            RestAssured
                .given()
                .filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(OppdragsRequest(kodeFagOmraade = "BP", attestert = false))
                .port(PORT)
                .post("$ATTESTASJON_BASE_API_PATH/sok")
                .then()
                .assertThat()
                .statusCode(HttpStatusCode.OK.value)
        }

        test("sok etter oppdrag med dummy token returnerer 500 Internal Server Error") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "dummytoken")
                    .body(GjelderIdRequest(gjelderId = "123456789"))
                    .port(PORT)
                    .post("$ATTESTASJON_BASE_API_PATH/sok")
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
                    path = "$ATTESTASJON_BASE_API_PATH/sok",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
        }

        test("hent alle fagområder returnerer 200 OK") {

            val fagOmraadeList =
                listOf(
                    FagOmraade(
                        navn = "Barnepensjon",
                        kode = "BP",
                    ),
                )

            every { attestasjonService.getFagOmraade() } returns fagOmraadeList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$ATTESTASJON_BASE_API_PATH/fagomraader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<FagOmraade>>(response.body.asString()) shouldBe fagOmraadeList
        }

        test("hent alle fagområder returnerer 500 Internal Server Error") {

            every { attestasjonService.getFagOmraade() } throws RuntimeException("Noe gikk galt")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$ATTESTASJON_BASE_API_PATH/fagomraader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.InternalServerError.value,
                    error = HttpStatusCode.InternalServerError.description,
                    message = "Noe gikk galt",
                    path = "$ATTESTASJON_BASE_API_PATH/fagomraader",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
        }

        test("søk etter oppdragsId på oppdragsdetaljer returnerer 200 OK") {

            val oppdragsDetaljerDto =
                OppdragsdetaljerDTO(
                    listOf(
                        OppdragslinjeDTO(
                            Oppdragslinje(
                                false,
                                LocalDate.parse("2000-01-01"),
                                null,
                                "FYL20170501007247481 79947001",
                                "FUBAR",
                                1,
                                12345678,
                                1234.56,
                                "MND",
                                "1234567",
                                50,
                                "12345678901",
                                "00123456789",
                                "12345612345",
                                "12345612345",
                            ),
                            null,
                            null,
                            listOf(
                                Attestasjon("X999123", LocalDate.parse("2050-01-01")),
                            ),
                        ),
                    ),
                    "X313373",
                )

            coEvery { attestasjonService.getOppdragsdetaljer(any(), any()) } returns oppdragsDetaljerDto

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$ATTESTASJON_BASE_API_PATH/12341234/oppdragsdetaljer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<OppdragsdetaljerDTO>(response.body.asString()) shouldBe oppdragsDetaljerDto
        }

        test("søk etter oppdragsId på oppdragsdetaljer returnerer 500 Internal Server Error") {

            coEvery { attestasjonService.getOppdragsdetaljer(any(), any()) } throws RuntimeException("Noe gikk galt")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$ATTESTASJON_BASE_API_PATH/12341234/oppdragsdetaljer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.InternalServerError.value,
                    error = HttpStatusCode.InternalServerError.description,
                    message = "Noe gikk galt",
                    path = "$ATTESTASJON_BASE_API_PATH/12341234/oppdragsdetaljer",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
        }

        test("attestering av oppdrag returnerer 200 OK") {

            val request =
                AttestasjonRequest(
                    "123456789",
                    "98765432100",
                    "BEH",
                    123456789,
                    listOf(
                        AttestasjonLinje(
                            99999,
                            "2021-01-01",
                        ),
                    ),
                )

            val zOsResponse =
                ZOsResponse(
                    "Oppdatering vellykket. 99999 linjer oppdatert",
                )

            coEvery { attestasjonService.attestereOppdrag(any(), any()) } returns zOsResponse

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .body(request)
                    .post("$ATTESTASJON_BASE_API_PATH/attestere")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<ZOsResponse>(response.body.asString()) shouldBe zOsResponse
        }

        test("attestering av oppdrag returnerer 500 Internal Server Error") {

            val request =
                AttestasjonRequest(
                    "123456789",
                    "98765432100",
                    "BEH",
                    123456789,
                    listOf(
                        AttestasjonLinje(
                            99999,
                            "2021-01-01",
                        ),
                    ),
                )

            coEvery { attestasjonService.attestereOppdrag(any(), any()) } throws RuntimeException("Ukjent feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .body(request)
                    .post("$ATTESTASJON_BASE_API_PATH/attestere")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    status = HttpStatusCode.InternalServerError.value,
                    error = HttpStatusCode.InternalServerError.description,
                    message = "Ukjent feil",
                    path = "$ATTESTASJON_BASE_API_PATH/attestere",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
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
