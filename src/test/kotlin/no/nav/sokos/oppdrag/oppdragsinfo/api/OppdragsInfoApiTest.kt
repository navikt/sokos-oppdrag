package no.nav.sokos.oppdrag.oppdragsinfo.api

import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
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
import no.nav.sokos.oppdrag.attestasjon.OPPDRAGSINFO_BASE_API_PATH
import no.nav.sokos.oppdrag.attestasjon.Testdata.tokenWithNavIdent
import no.nav.sokos.oppdrag.common.dto.WrappedReponseWithErrorDTO
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsEnhetDTO
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsLinjeDetaljerDTO
import no.nav.sokos.oppdrag.oppdragsinfo.service.OppdragsInfoService

private const val PORT = 9090
private const val OPPDRAGS_ID = "123"
private const val LINJE_ID = "1"

private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

private val validationFilter = OpenApiValidationFilter("openapi/oppdragsinfo-v1-swagger.yaml")
private val oppdragsInfoService = mockk<OppdragsInfoService>()

@OptIn(ExperimentalTime::class)
internal class OppdragsInfoApiTest :
    FunSpec({

        beforeTest {
            server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
        }

        afterTest {
            server.stop(5, 5)
        }

        test("søk etter oppdragsegenskaper med gyldig gjelderId returnerer 200 OK") {
            val oppdragsegenskaperList =
                listOf(
                    Oppdrag(
                        fagsystemId = "12345678901",
                        oppdragsId = 1234556,
                        navnFaggruppe = "faggruppeNavn",
                        navnFagomraade = "fagomraadeNavn",
                        kjorIdag = "kjorIdag",
                        typeBilag = "bilagsType",
                        kodeStatus = "PASS",
                    ),
                )

            coEvery { oppdragsInfoService.getOppdrag(any(), any(), any()) } returns WrappedReponseWithErrorDTO(data = oppdragsegenskaperList)

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(OppdragsRequest(gjelderId = "12345678901", fagGruppeKode = ""))
                    .port(PORT)
                    .post("$OPPDRAGSINFO_BASE_API_PATH/sok")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            val result = Json.decodeFromString<WrappedReponseWithErrorDTO<Oppdrag>>(response.asString())
            result.data shouldBe oppdragsegenskaperList
            result.data.size shouldBe 1
        }

        test("sok etter oppdragsegenskaper med ugyldig gjelderId returnerer 400 Bad Request") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .body(OppdragsRequest(gjelderId = "123"))
                    .port(PORT)
                    .post("$OPPDRAGSINFO_BASE_API_PATH/sok")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.BadRequest.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.BadRequest.description,
                    status = HttpStatusCode.BadRequest.value,
                    message = "gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer",
                    path = "$OPPDRAGSINFO_BASE_API_PATH/sok",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("sok etter oppdragsegenskaper med dummy token returnerer 500 Internal Server Error") {

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "dummytoken")
                    .body(OppdragsRequest(gjelderId = "12345678901"))
                    .port(PORT)
                    .post("$OPPDRAGSINFO_BASE_API_PATH/sok")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.InternalServerError.value)
                    .extract()
                    .response()

            Json.decodeFromString<ApiError>(response.asString()) shouldBe
                ApiError(
                    error = HttpStatusCode.InternalServerError.description,
                    status = HttpStatusCode.InternalServerError.value,
                    message = "The token was expected to have 3 parts, but got 0.",
                    path = "$OPPDRAGSINFO_BASE_API_PATH/sok",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent oppdragslinjer med gyldig gjelderId returnerer 200 OK") {

            val oppdragsLinjeList =
                listOf(
                    OppdragsLinje(
                        linjeId = 11,
                        kodeKlasse = "ABC",
                        datoVedtakFom = "2024-01-01",
                        datoVedtakTom = null,
                        sats = 99.9,
                        typeSats = "DAG",
                        kodeStatus = "X",
                        datoFom = "2024-01-01",
                        linjeIdKorr = 22,
                        attestert = "J",
                        delytelseId = "D3",
                        utbetalesTilId = "A1B2",
                        refunderesId = "123456789",
                        brukerId = "abc123",
                        tidspktReg = "2024-01-01",
                        vedtakssats = 1000.0,
                        hovedkontonr = "TB",
                        underkontonr = "AFP",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjer(any()) } returns oppdragsLinjeList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/oppdragslinjer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<OppdragsLinje>>(response.asString()) shouldBe oppdragsLinjeList
        }

        test("hent oppdragslinjer returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjer(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/oppdragslinjer")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/oppdragslinjer",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent behandlende enheter for en oppdragsId returnerer 200 OK") {

            val behandlendeEnhetList =
                OppdragsEnhetDTO(
                    enhet =
                        OppdragsEnhet(
                            typeEnhet = "BOS",
                            datoFom = "2024-01-01",
                            enhet = "0502",
                        ),
                    behandlendeEnhet =
                        OppdragsEnhet(
                            typeEnhet = "BEH",
                            datoFom = "2024-01-01",
                            enhet = "0502",
                        ),
                )

            every { oppdragsInfoService.getBehandlendeEnhetForOppdrag(any()) } returns behandlendeEnhetList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enheter")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<OppdragsEnhetDTO>(response.asString()) shouldBe behandlendeEnhetList
        }

        test("hent behandlende enheter for en oppdragsId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getBehandlendeEnhetForOppdrag(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enheter")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enheter",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent omposteringer for en oppdragsId returnerer 200 OK") {

            val omposteringerList =
                listOf(
                    Ompostering(
                        gjelderId = "a1",
                        kodeFaggruppe = "fag1",
                        lopenr = 1,
                        ompostering = "z",
                        datoOmposterFom = "2024-01-01",
                        feilReg = "",
                        beregningsId = 22,
                        utfort = "j",
                        brukerid = "abc123",
                        tidspktReg = "2024-01-01",
                    ),
                )

            every { oppdragsInfoService.getOppdragsOmposteringer(any()) } returns omposteringerList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/omposteringer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Ompostering>>(response.asString()) shouldBe omposteringerList
        }

        test("hent omposteringer for en oppdragsId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsOmposteringer(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/omposteringer")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/omposteringer",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent enhetshistorikk for oppdragsId returnerer 200 OK") {

            val oppdragsEnhetList =
                listOf(
                    OppdragsEnhet(
                        typeEnhet = "BOS",
                        datoFom = "2024-01-01",
                        enhet = "0502",
                    ),
                )

            every { oppdragsInfoService.getOppdragsEnhetsHistorikk(any()) } returns oppdragsEnhetList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enhetshistorikk")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<OppdragsEnhet>>(response.asString()) shouldBe oppdragsEnhetList
        }

        test("hent enhetshistorikk for oppdragsId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsEnhetsHistorikk(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enhetshistorikk")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enhetshistorikk",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent statushistorikk for oppdragsId returnerer 200 OK") {

            val oppdragsStatusList =
                listOf(
                    OppdragsStatus(
                        kodeStatus = "AKTIV",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsStatusHistorikk(any()) } returns oppdragsStatusList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/statushistorikk")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<OppdragsStatus>>(response.asString()) shouldBe oppdragsStatusList
        }

        test("hent statushistorikk for oppdragsId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsStatusHistorikk(any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/statushistorikk")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/statushistorikk",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent linjestatus i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val linjeStatusList =
                listOf(
                    LinjeStatus(
                        kodeStatus = "AKTIV",
                        datoFom = "2024-01-01",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeStatuser(any(), any()) } returns linjeStatusList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/statuser")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<LinjeStatus>>(response.asString()) shouldBe linjeStatusList
        }

        test("hent linjestatus i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeStatuser(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/statuser")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/statuser",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent attestant i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val attestantList =
                listOf(
                    Attestant(
                        attestantId = "A1",
                        datoUgyldigFom = "2024-01-01",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeAttestanter(any(), any()) } returns attestantList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/attestanter")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Attestant>>(response.asString()) shouldBe attestantList
        }

        test("hent attestant i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeAttestanter(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/attestanter")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/attestanter",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent detaljer i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val oppdragsLinjeDetaljerDTO =
                OppdragsLinjeDetaljerDTO(
                    korrigerteLinjeIder =
                        listOf(
                            OppdragsLinje(
                                linjeId = 11,
                                kodeKlasse = "ABC",
                                datoVedtakFom = "2024-01-01",
                                datoVedtakTom = null,
                                sats = 99.9,
                                typeSats = "DAG",
                                kodeStatus = "X",
                                datoFom = "2024-01-01",
                                linjeIdKorr = 22,
                                attestert = "J",
                                delytelseId = "D3",
                                utbetalesTilId = "A1B2",
                                refunderesId = "123456789",
                                brukerId = "abc123",
                                tidspktReg = "2024-01-01",
                                vedtakssats = 1500.0,
                                hovedkontonr = "216",
                                underkontonr = "5310",
                            ),
                        ),
                    harValutaer = TRUE,
                    harSkyldnere = TRUE,
                    harKravhavere = TRUE,
                    harEnheter = TRUE,
                    harGrader = TRUE,
                    harTekster = TRUE,
                    harKidliste = TRUE,
                    harMaksdatoer = FALSE,
                )

            every { oppdragsInfoService.getOppdragsLinjeDetaljer(any(), any()) } returns oppdragsLinjeDetaljerDTO

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/detaljer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<OppdragsLinjeDetaljerDTO>(response.asString()) shouldBe oppdragsLinjeDetaljerDTO
        }

        test("hent detaljer i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeDetaljer(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/detaljer")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/detaljer",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent valuta i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {
            val valutaList =
                listOf(
                    Valuta(
                        linjeId = 1,
                        typeValuta = "NOK",
                        datoFom = "2024-01-01",
                        nokkelId = 2,
                        valuta = "NOK",
                        feilreg = "",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeValutaer(any(), any()) } returns valutaList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/valutaer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Valuta>>(response.asString()) shouldBe valutaList
        }

        test("hent valuta i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeValutaer(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/valutaer")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/valutaer",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent skyldner i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

            val skyldnerList =
                listOf(
                    Skyldner(
                        linjeId = 1,
                        skyldnerId = "BB",
                        datoFom = "2024-01-01",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeSkyldnere(any(), any()) } returns skyldnerList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/skyldnere")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Skyldner>>(response.asString()) shouldBe skyldnerList
        }

        test("hent skyldner i kombinasjon med oppdragsId og linjeId skal returnere 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeSkyldnere(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/skyldnere")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/skyldnere",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent kravhaver i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val kravhaverList =
                listOf(
                    Kravhaver(
                        linjeId = 1,
                        kravhaverId = "ABC",
                        datoFom = "2024-01-01",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeKravhavere(any(), any()) } returns kravhaverList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kravhavere")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Kravhaver>>(response.asString()) shouldBe kravhaverList
        }

        test("hent kravhaver i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeKravhavere(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kravhavere")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/kravhavere",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent enhet i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val linjeEnhetList =
                listOf(
                    LinjeEnhet(
                        linjeId = 1,
                        typeEnhet = "BOS",
                        enhet = "",
                        datoFom = "2024-01-01",
                        nokkelId = 123,
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeEnheter(any(), any()) } returns linjeEnhetList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/enheter")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<LinjeEnhet>>(response.asString()) shouldBe linjeEnhetList
        }

        test("hent enhet i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeEnheter(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/enheter")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/enheter",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent grad i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val gradList =
                listOf(
                    Grad(
                        linjeId = 1,
                        typeGrad = "ABC",
                        grad = 123,
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeGrader(any(), any()) } returns gradList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/grader")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Grad>>(response.asString()) shouldBe gradList
        }

        test("hent grad i kombinasjon med oppdragsId og linjeId skal returnere 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeGrader(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/grader")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/grader",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent tekst i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val tekstList =
                listOf(
                    Tekst(
                        linjeId = 1,
                        tekst = "asd",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeTekster(any(), any()) } returns tekstList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/tekster")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Tekst>>(response.asString()) shouldBe tekstList
        }

        test("hent tekst i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeTekster(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/tekster")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/tekster",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent kid i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val kidList =
                listOf(
                    Kid(
                        linjeId = 1,
                        kid = "ABC",
                        datoFom = "2024-01-01",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeKid(any(), any()) } returns kidList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kid")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Kid>>(response.asString()) shouldBe kidList
        }

        test("hent kid i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeKid(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kid")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/kid",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent maksdato i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val maksdatoList =
                listOf(
                    Maksdato(
                        linjeId = 1,
                        maksdato = "2024-01-01",
                        datoFom = "2024-01-01",
                        tidspktReg = "2024-01-01",
                        brukerid = "A12345",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeMaksDatoer(any(), any()) } returns maksdatoList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/maksdatoer")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Maksdato>>(response.asString()) shouldBe maksdatoList
        }

        test("hent maksdato i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeMaksDatoer(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/maksdatoer")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/maksdatoer",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }

        test("hent øvrig i kombinasjon med oppdragsId og linjeId returnerer 200 OK") {

            val ovrigList =
                listOf(
                    Ovrig(
                        linjeId = 1,
                        vedtaksId = "b123",
                        henvisning = "c321",
                        typeSoknad = "NN",
                    ),
                )

            every { oppdragsInfoService.getOppdragsLinjeOvriger(any(), any()) } returns ovrigList

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/ovrig")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatusCode.OK.value)
                    .extract()
                    .response()

            Json.decodeFromString<List<Ovrig>>(response.asString()) shouldBe ovrigList
        }

        test("hent øvrig i kombinasjon med oppdragsId og linjeId returnerer 500 Internal Server Error") {

            every { oppdragsInfoService.getOppdragsLinjeOvriger(any(), any()) } throws RuntimeException("En feil")

            val response =
                RestAssured
                    .given()
                    .filter(validationFilter)
                    .header(HttpHeaders.ContentType, APPLICATION_JSON)
                    .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                    .port(PORT)
                    .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/ovrig")
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
                    path = "$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/1/ovrig",
                    timestamp = Instant.parse(response.body.jsonPath().getString("timestamp")),
                )
        }
    })

private fun Application.applicationTestModule() {
    commonConfig()
    routing {
        authenticate(false, AUTHENTICATION_NAME) {
            oppdragsInfoApi(oppdragsInfoService)
        }
    }
}
