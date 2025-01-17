package no.nav.sokos.oppdrag.fastedata.api

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
import io.mockk.mockk
import io.restassured.RestAssured

import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.FASTEDATA_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.dto.KorrigeringsaarsakDTO
import no.nav.sokos.oppdrag.fastedata.service.FasteDataService

private const val PORT = 9090

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

        test("fagområder returnerer 200 OK") {

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
        test("korrigeringsårsaker returnerer 200 OK") {

            coEvery { fasteDataService.getKorrigeringsaarsaker(any()) } returns korrigeringsaarsaker

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/MYSTB/korrigeringsaarsaker")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<KorrigeringsaarsakDTO>>(response.asString()) shouldBe korrigeringsaarsaker
        }
        test("korrigeringsårsaker validerer fagområde") {

            coEvery { fasteDataService.getKorrigeringsaarsaker(any()) } returns korrigeringsaarsaker

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$FASTEDATA_BASE_API_PATH/burde være ugyldig verd\' or 1=1 or '' = \'/korrigeringsaarsaker")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.BadRequest.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<KorrigeringsaarsakDTO>>(response.asString()) shouldBe korrigeringsaarsaker
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

val fagomraader =
    listOf(
        Fagomraade(
            antallAttestanter = 1,
            anviser = "lol",
            bilagstypeFinnes = true,
            klassekodeFinnes = true,
            kodeFagomraade = "lol",
            kodeFaggruppe = "lol",
            kodeMotregningsgruppe = "lol",
            korraarsakFinnes = true,
            maksAktiveOppdrag = 4,
            navnFagomraade = "lol",
            regelFinnes = true,
            sjekkMotTps = "lol",
            sjekkOffId = "lol",
            tpsDistribusjon = "lol",
        ),
        Fagomraade(
            antallAttestanter = 2,
            anviser = "test",
            bilagstypeFinnes = false,
            klassekodeFinnes = false,
            kodeFagomraade = "test",
            kodeFaggruppe = "test",
            kodeMotregningsgruppe = "test",
            korraarsakFinnes = false,
            maksAktiveOppdrag = 5,
            navnFagomraade = "test",
            regelFinnes = false,
            sjekkMotTps = "test",
            sjekkOffId = "test",
            tpsDistribusjon = "test",
        ),
        Fagomraade(
            antallAttestanter = 3,
            anviser = "example",
            bilagstypeFinnes = true,
            klassekodeFinnes = true,
            kodeFagomraade = "example",
            kodeFaggruppe = "example",
            kodeMotregningsgruppe = "example",
            korraarsakFinnes = true,
            maksAktiveOppdrag = 6,
            navnFagomraade = "example",
            regelFinnes = true,
            sjekkMotTps = "example",
            sjekkOffId = "example",
            tpsDistribusjon = "example",
        ),
    )

val korrigeringsaarsaker =
    listOf(
        KorrigeringsaarsakDTO(
            navn = "Linjestatus endret",
            kode = "0001",
            medforerKorrigering = true,
        ),
    )
