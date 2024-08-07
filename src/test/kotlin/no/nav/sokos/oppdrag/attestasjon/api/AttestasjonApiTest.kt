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
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsIdRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.common.model.GjelderIdRequest
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
        val oppdragsListe =
            listOf(
                Oppdrag(
                    gjelderId = "12345678901",
                    navnFagGruppe = "navnFaggruppe",
                    navnFagOmraade = "navnFagomraade",
                    oppdragsId = 987654,
                    fagsystemId = "123456789",
                ),
            )

        every { attestasjonService.getOppdrag(any(), any(), any(), any(), any(), any()) } returns oppdragsListe

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(GjelderIdRequest("123456789"))
                .port(PORT)
                .post("$ATTESTASJON_BASE_API_PATH/sok")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Oppdrag>("oppdragsId").first().shouldBe(987654)
    }

    test("søk etter oppdragsId på oppdragslinjer endepunktet skal returnere 200 OK") {
        val oppdragsDetaljerListe =
            listOf(
                OppdragsDetaljer(
                    klasse = "klasse",
                    delytelsesId = "delytelsesId",
                    sats = 123.45,
                    satstype = "satstype",
                    datoVedtakFom = "2021-01-01",
                    datoVedtakTom = "2021-12-31",
                    attestant = "attestant",
                    navnFagOmraade = "navnFagOmraade",
                    fagsystemId = "123456789",
                ),
            )

        every { attestasjonService.getOppdragsDetaljer(any()) } returns oppdragsDetaljerListe

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(OppdragsIdRequest(listOf(987654)))
                .port(PORT)
                .post("$ATTESTASJON_BASE_API_PATH/oppdragsdetaljer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Int>("fagsystemId").first().shouldBe("123456789")
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
