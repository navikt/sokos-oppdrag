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
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponse
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import org.hamcrest.Matchers.equalTo

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

    test("søk etter oppdrag med gyldig gjelderId skal returnere 200 OK") {
        val oppdragsListe =
            listOf(
                Oppdrag(
                    ansvarsSted = "1337",
                    fagsystemId = "123456789",
                    gjelderId = "12345678901",
                    kostnadsSted = "8128",
                    navnFagGruppe = "navnFaggruppe",
                    navnFagOmraade = "navnFagomraade",
                    oppdragsId = 987654,
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

    test("søk etter oppdragsId på oppdragslinjer endepunktet skal returnere 200 OK") {
        val oppdragsDetaljerListe =
            listOf(
                OppdragsDetaljer(
                    ansvarsStedForOppdrag = "1337",
                    antallAttestanter = 1,
                    attestant = "attestant",
                    datoVedtakFom = "2021-01-01",
                    datoVedtakTom = "2021-12-31",
                    delytelsesId = "delytelsesId",
                    fagSystemId = "123456789",
                    kodeKlasse = "KLASSE",
                    kostnadsStedForOppdrag = "8128",
                    linjeId = "1",
                    navnFagGruppe = "Aliens",
                    navnFagOmraade = "Area 51",
                    oppdragGjelderId = "123456789",
                    oppdragsId = "12345678",
                    sats = 123.45,
                    satstype = "satstype",
                ),
            )

        every { attestasjonService.getOppdragsDetaljer(any()) } returns oppdragsDetaljerListe

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$ATTESTASJON_BASE_API_PATH/oppdragsdetaljer/12341234")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Int>("fagSystemId").first().shouldBe("123456789")
    }

    // TODO: En test for .get("$ATTESTASJON_BASE_API_PATH/oppdragsdetaljer/12341234") som returnerer 400 Bad Request??

    test("attestering av oppdrag skal returnere 200 OK") {

        val request =
            AttestasjonRequest(
                "123456789",
                "BP",
                999999999,
                "Z1234567",
                false,
                listOf(
                    AttestasjonLinje(
                        99999,
                        "Z1234567",
                        "2021-01-01",
                    ),
                ),
            )

        val zOsResponse =
            PostOSAttestasjonResponse200(
                osAttestasjonOperationResponse =
                    PostOSAttestasjonResponse200OSAttestasjonOperationResponse(
                        attestasjonskvittering =
                            PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering(
                                responsAttestasjon =
                                    PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon(
                                        gjelderId = "123456789",
                                        oppdragsId = 999_999_999,
                                        antLinjerMottatt = 99_999,
                                        statuskode = 99,
                                        melding = "Test melding",
                                    ),
                            ),
                    ),
            )

        coEvery { attestasjonService.attestereOppdrag(any()) } returns zOsResponse

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

        response.body.jsonPath().getString("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.GjelderId")
            .shouldBe("123456789")
        response.body.jsonPath().getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.OppdragsId")
            .shouldBe(999_999_999)
        response.body.jsonPath().getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.AntLinjerMottatt")
            .shouldBe(99_999)
        response.body.jsonPath().getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.Statuskode")
            .shouldBe(99)
        response.body.jsonPath().getString("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.Melding")
            .shouldBe("Test melding")
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
