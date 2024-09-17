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
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragslinjePlain
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponse
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import org.hamcrest.Matchers.equalTo
import java.time.LocalDate

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
                    antallAttestanter = 1,
                    fagSystemId = "123456789",
                    gjelderId = "12345678901",
                    kostnadsSted = "8128",
                    fagGruppe = "navnFaggruppe",
                    fagOmraade = "navnFagomraade",
                    oppdragsId = 987654,
                    kodeFagOmraade = "kodeFagomraade",
                    kodeFagGruppe = "kodeFaggruppe",
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
        every { attestasjonService.getOppdragsDetaljer(any()) } returns
            listOf(
                OppdragsDetaljer(
                    ansvarsStedForOppdrag = "1337",
                    antallAttestanter = 1,
                    fagGruppe = "faggruppe",
                    fagOmraade = "fagområde",
                    fagSystemId = "123456789",
                    gjelderId = "12345612345",
                    kodeFagOmraade = "FUBAR",
                    kostnadsStedForOppdrag = "8128",
                    linjer =
                        listOf(
                            Oppdragslinje(
                                oppdragsLinje =
                                    OppdragslinjePlain(
                                        attestert = false,
                                        datoVedtakFom = LocalDate.parse("2000-01-01"),
                                        datoVedtakTom = null,
                                        delytelseId = 123,
                                        kodeKlasse = "FUBAR",
                                        linjeId = 1,
                                        oppdragsId = 12345678,
                                        sats = 1234.56,
                                        typeSats = "MND",
                                    ),
                                attestasjoner =
                                    listOf(
                                        Attestasjon(attestant = "X999123", datoUgyldigFom = LocalDate.parse("2050-01-01")),
                                    ),
                                ansvarsStedForOppdragsLinje = null,
                                kostnadsStedForOppdragsLinje = null,
                            ),
                        ),
                    oppdragsId = "12345678",
                ),
            )

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

        response.body.jsonPath()
            .getString("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.GjelderId")
            .shouldBe("123456789")
        response.body.jsonPath()
            .getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.OppdragsId")
            .shouldBe(999_999_999)
        response.body.jsonPath()
            .getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.AntLinjerMottatt")
            .shouldBe(99_999)
        response.body.jsonPath()
            .getInt("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.Statuskode")
            .shouldBe(99)
        response.body.jsonPath()
            .getString("OSAttestasjonOperationResponse.Attestasjonskvittering.ResponsAttestasjon.Melding")
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
