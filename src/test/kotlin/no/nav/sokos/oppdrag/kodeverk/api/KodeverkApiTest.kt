package no.nav.sokos.oppdrag.kodeverk.api

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
import io.mockk.mockk
import io.restassured.RestAssured

import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.KODERVERK_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.kodeverk.domain.FagGruppe
import no.nav.sokos.oppdrag.kodeverk.domain.FagOmraade
import no.nav.sokos.oppdrag.kodeverk.service.KodeverkService

private const val PORT = 9090

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/kodeverk-v1-swagger.yaml")
private val kodeverkService = mockk<KodeverkService>()

internal class KodeverkApiTest :
    FunSpec({

        beforeTest {
            server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
        }

        afterTest {
            server.stop(5, 5)
        }

        test("hent faggrupper returnerer 200 OK") {

            val fagGruppeKodeList =
                listOf(
                    FagGruppe(
                        navn = "ABC",
                        type = "DEF",
                    ),
                )

            coEvery { kodeverkService.getFagGrupper() } returns fagGruppeKodeList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer ${Testdata.tokenWithNavIdent}")
                    .port(PORT)
                    .get("$KODERVERK_BASE_API_PATH/faggrupper")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<FagGruppe>>(response.asString()) shouldBe fagGruppeKodeList
        }

        test("hent faggrupper returnerer 500 Internal Server Error") {

            coEvery { kodeverkService.getFagGrupper() } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer ${Testdata.tokenWithNavIdent}")
                    .port(PORT)
                    .get("$KODERVERK_BASE_API_PATH/faggrupper")
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
                    path = "$KODERVERK_BASE_API_PATH/faggrupper",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent alle fagområder returnerer 200 OK") {

            val fagOmraadeList =
                listOf(
                    FagOmraade(
                        navnFagomraade = "Barnepensjon",
                        kodeFagomraade = "BP",
                    ),
                )

            coEvery { kodeverkService.getFagOmraader() } returns fagOmraadeList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer ${Testdata.tokenWithNavIdent}")
                    .port(PORT)
                    .get("$KODERVERK_BASE_API_PATH/fagomraader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<FagOmraade>>(response.body.asString()) shouldBe fagOmraadeList
        }

        test("hent alle fagområder returnerer 500 Internal Server Error") {

            coEvery { kodeverkService.getFagOmraader() } throws RuntimeException("Noe gikk galt")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer ${Testdata.tokenWithNavIdent}")
                    .port(PORT)
                    .get("$KODERVERK_BASE_API_PATH/fagomraader")
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
                    path = "$KODERVERK_BASE_API_PATH/fagomraader",
                    timestamp = Instant.parse(response.jsonPath().getString("timestamp")),
                )
        }
    })

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            kodeverkApi(kodeverkService)
        }
    }
}
