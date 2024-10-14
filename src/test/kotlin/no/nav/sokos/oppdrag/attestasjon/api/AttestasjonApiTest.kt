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
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.ATTESTASJON_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZOsResponse
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import org.hamcrest.Matchers.equalTo
import java.time.LocalDate

private const val PORT = 9090

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/attestasjon-v1-swagger.yaml")
private val attestasjonService = mockk<AttestasjonService>()

internal class AttestasjonApiTest : FunSpec({

    beforeTest {
        server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
    }

    afterTest {
        server.stop(5, 5)
    }

    test("søk etter oppdrag med gyldig gjelderId skal returnere 200 OK") {
        val oppdragsListe =
            listOf(
                Oppdrag(
                    "1337",
                    1,
                    "navnFaggruppe",
                    "navnFagomraade",
                    "123456789",
                    "12345678901",
                    "kodeFaggruppe",
                    "kodeFagomraade",
                    "8128",
                    987654,
                ),
            )

        every { attestasjonService.getOppdrag(any(), any(), any(), any(), any(), any()) } returns oppdragsListe

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(OppdragsRequest(gjelderId = "123456789"))
                .port(PORT)
                .post("$ATTESTASJON_BASE_API_PATH/sok")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Oppdrag>("oppdragsId").first().shouldBe(987654)
    }

    test("sok etter oppdrag med ugyldig gjelderId skal returnere 400 Bad Request") {

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(OppdragsRequest(gjelderId = "123"))
            .port(PORT)
            .post("$ATTESTASJON_BASE_API_PATH/sok")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer"))
    }

    test("sok etter oppdrag med ugyldige søkeparametere skal returnere 400 Bad Request") {

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(OppdragsRequest(kodeFagGruppe = "BP", attestert = true))
            .port(PORT)
            .post("$ATTESTASJON_BASE_API_PATH/sok")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("Ugyldig kombinasjon av søkeparametere"))
    }

    test("sok etter oppdrag med gyldig søkeparametere skal returnere 200 OK") {

        every { attestasjonService.getOppdrag(any(), any(), any(), any(), any(), any()) } returns emptyList()

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(OppdragsRequest(kodeFagOmraade = "BP", attestert = false))
            .port(PORT)
            .post("$ATTESTASJON_BASE_API_PATH/sok")
            .then().assertThat()
            .statusCode(HttpStatusCode.OK.value)
    }

    test("hent alle fagområder skal returnere 200 OK") {

        val fagOmraade =
            FagOmraade(
                navn = "Barnepensjon",
                kode = "BP",
            )

        every { attestasjonService.getFagOmraade() } returns listOf(fagOmraade)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$ATTESTASJON_BASE_API_PATH/fagomraader")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<FagOmraade>("navn").first().shouldBe("Barnepensjon")
        response.body.jsonPath().getList<FagOmraade>("kode").first().shouldBe("BP")
    }

    test("søk etter oppdragsId på oppdragsdetaljer endepunktet skal returnere 200 OK") {
        every { attestasjonService.getOppdragsdetaljer(any(), any()) } returns
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

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$ATTESTASJON_BASE_API_PATH/12341234/oppdragsdetaljer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Int>("linjer").size shouldBe 1
    }

    // TODO: En test for .get("$ATTESTASJON_BASE_API_PATH/oppdragsdetaljer/12341234") som returnerer 400 Bad Request??

    test("attestering av oppdrag skal returnere 200 OK") {

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
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .body(request)
                .post("$ATTESTASJON_BASE_API_PATH/attestere")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body().jsonPath().getString("message") shouldBe "Oppdatering vellykket. 99999 linjer oppdatert"
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
