package no.nav.sokos.oppdrag.oppdragsinfo.api

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
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
import no.nav.sokos.oppdrag.OPPDRAGSINFO_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
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
import org.hamcrest.Matchers.equalTo
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

private const val PORT = 9090
private const val OPPDRAGS_ID = "123"
private const val LINJE_ID = "1"

private lateinit var server: NettyApplicationEngine

private val validationFilter = OpenApiValidationFilter("openapi/oppdragsinfo-v1-swagger.yaml")
private val oppdragsInfoService = mockk<OppdragsInfoService>()

internal class OppdragsInfoApiTest : FunSpec({

    beforeTest {
        server = embeddedServer(Netty, PORT, module = Application::applicationTestModule).start()
    }

    afterTest {
        server.stop(5, 5)
    }

    test("søk etter oppdragsegenskaper med gyldig gjelderId skal returnere 200 OK") {
        val oppdragsegenskaperList =
            listOf(
                Oppdrag(
                    fagsystemId = "12345678901",
                    oppdragsId = 1234556,
                    navnFagGruppe = "faggruppeNavn",
                    navnFagomraade = "fagomraadeNavn",
                    kjorIdag = "kjorIdag",
                    typeBilag = "bilagsType",
                    kodeStatus = "PASS",
                ),
            )

        every { oppdragsInfoService.getOppdrag(any(), any(), any()) } returns oppdragsegenskaperList

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(OppdragsRequest(gjelderId = "12345678901", faggruppeKode = "ABC"))
                .port(PORT)
                .post("$OPPDRAGSINFO_BASE_API_PATH/sok")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract()
                .response()

        response.body.jsonPath().getList<Oppdrag>("").shouldHaveSize(1)
    }

    test("sok etter oppdragsegenskaper med ugyldig gjelderId skal returnere 400 Bad Request") {

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(OppdragsRequest(gjelderId = "123", faggruppeKode = ""))
            .port(PORT)
            .post("$OPPDRAGSINFO_BASE_API_PATH/sok")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer"))
    }

    test("hent oppdragslinjer med gyldig gjelderId skal returnere 200 OK") {

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
                    refunderesOrgnr = "123456789",
                    brukerId = "abc123",
                    tidspktReg = "2024-01-01",
                ),
            )

        every { oppdragsInfoService.getOppdragsLinjer(any()) } returns oppdragsLinjeList

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/oppdragslinjer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<OppdragsLinje>("").shouldHaveSize(1)
    }

    test("hent alle faggrupper skal returnere 200 OK") {

        val faggruppe =
            FagGruppe(
                navn = "ABC",
                type = "DEF",
            )

        every { oppdragsInfoService.getFagGrupper() } returns listOf(faggruppe)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/faggrupper")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<FagGruppe>("navn").first().shouldBe("ABC")
        response.body.jsonPath().getList<FagGruppe>("type").first().shouldBe("DEF")
    }

    test("hent behandlende enheter for en oppdragsId skal returnere 200 OK") {

        val behandlendeEnhet =
            OppdragsEnhetDTO(
                enhet =
                    OppdragsEnhet(
                        type = "BOS",
                        datoFom = "2024-01-01",
                        enhet = "0502",
                    ),
                behandlendeEnhet =
                    OppdragsEnhet(
                        type = "BEH",
                        datoFom = "2024-01-01",
                        enhet = "0502",
                    ),
            )

        every { oppdragsInfoService.getBehandlendeEnhetForOppdrag(any()) } returns behandlendeEnhet

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enheter")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getJsonObject<OppdragsEnhetDTO>("enhet.type").shouldBe("BOS")
        response.body.jsonPath().getJsonObject<OppdragsEnhet>("behandlendeEnhet.type").shouldBe("BEH")
    }

    test("hent omposteringer for en oppdragsId skal returnere 200 OK") {

        val omposteringerList =
            listOf(
                Ompostering(
                    id = "a1",
                    kodeFaggruppe = "fag1",
                    lopenr = 1,
                    ompostering = "z",
                    omposteringFom = "2024-01-01",
                    feilReg = "",
                    beregningsId = 22,
                    utfort = "j",
                    brukerid = "abc123",
                    tidspktReg = "2024-01-01",
                ),
            )

        every { oppdragsInfoService.getOppdragsOmposteringer(any()) } returns omposteringerList

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/omposteringer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Ompostering>("kodeFaggruppe").first().shouldBe("fag1")
        response.body.jsonPath().getList<Ompostering>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent enhetshistorikk for oppdragsId skal returnere 200 OK") {

        val oppdragsEnhet =
            OppdragsEnhet(
                type = "BOS",
                datoFom = "2024-01-01",
                enhet = "0502",
            )

        every { oppdragsInfoService.getOppdragsEnhetsHistorikk(any()) } returns listOf(oppdragsEnhet)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/enhetshistorikk")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<OppdragsEnhet>("type").first().shouldBe("BOS")
        response.body.jsonPath().getList<OppdragsEnhet>("datoFom").first().shouldBe("2024-01-01")
    }

    test("hent statushistorikk for oppdragsId skal returnere 200 OK") {

        val oppdragsStatus =
            OppdragsStatus(
                kodeStatus = "AKTIV",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsStatusHistorikk(any()) } returns listOf(oppdragsStatus)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/statushistorikk")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<OppdragsStatus>("kodeStatus").first().shouldBe("AKTIV")
        response.body.jsonPath().getList<OppdragsStatus>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent linjestatus i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val linjeStatus =
            LinjeStatus(
                status = "AKTIV",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeStatuser(any(), any()) } returns listOf(linjeStatus)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/statuser")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<LinjeStatus>("status").first().shouldBe("AKTIV")
        response.body.jsonPath().getList<LinjeStatus>("datoFom").first().shouldBe("2024-01-01")
    }

    test("hent attestant i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val attestant =
            Attestant(
                attestantId = "A1",
                ugyldigFom = "2024-01-01",
            )

        every { oppdragsInfoService.getOppdragsLinjeAttestanter(any(), any()) } returns listOf(attestant)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/attestanter")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Attestant>("attestantId").first().shouldBe("A1")
        response.body.jsonPath().getList<Attestant>("ugyldigFom").first().shouldBe("2024-01-01")
    }

    test("hent detaljer i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

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
                            refunderesOrgnr = "123456789",
                            brukerId = "abc123",
                            tidspktReg = "2024-01-01",
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
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/detaljer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        println(response.body.asString())

        response.body.jsonPath().getBoolean("harValutaer").shouldBe(TRUE)
        response.body.jsonPath().getBoolean("harMaksdatoer").shouldBe(FALSE)
        response.body.jsonPath().getList<Int>("korrigerteLinjeIder").shouldHaveSize(1)
    }

    test("hent valuta i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {
        val valuta =
            Valuta(
                linjeId = 1,
                type = "NOK",
                datoFom = "2024-01-01",
                nokkelId = 2,
                valuta = "NOK",
                feilreg = "",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeValutaer(any(), any()) } returns listOf(valuta)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/valutaer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Valuta>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Valuta>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent skyldner i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val skyldner =
            Skyldner(
                linjeId = 1,
                skyldnerId = "BB",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeSkyldnere(any(), any()) } returns listOf(skyldner)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/skyldnere")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Skyldner>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Skyldner>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent kravhaver i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val kravhaver =
            Kravhaver(
                linjeId = 1,
                kravhaverId = "ABC",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeKravhavere(any(), any()) } returns listOf(kravhaver)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kravhavere")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Kravhaver>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Kravhaver>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent enhet i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val linjeEnhet =
            LinjeEnhet(
                linjeId = 1,
                typeEnhet = "BOS",
                enhet = "",
                datoFom = "2024-01-01",
                nokkelId = 123,
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeEnheter(any(), any()) } returns listOf(linjeEnhet)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/enheter")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<LinjeEnhet>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<LinjeEnhet>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent grad i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val grad =
            Grad(
                linjeId = 1,
                typeGrad = "ABC",
                grad = 123,
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeGrader(any(), any()) } returns listOf(grad)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/grader")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Grad>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Grad>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent tekst i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val tekst =
            Tekst(
                linjeId = 1,
                tekst = "asd",
            )

        every { oppdragsInfoService.getOppdragsLinjeTekster(any(), any()) } returns listOf(tekst)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/tekster")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Tekst>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Tekst>("tekst").first().shouldBe("asd")
    }

    test("hent kid i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val kid =
            Kid(
                linjeId = 1,
                kid = "ABC",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeKid(any(), any()) } returns listOf(kid)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kid")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Kid>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Kid>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent maksdato i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val maksdato =
            Maksdato(
                linjeId = 1,
                maksdato = "2024-01-01",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.getOppdragsLinjeMaksDatoer(any(), any()) } returns listOf(maksdato)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/maksdatoer")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Maksdato>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Maksdato>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent øvrig i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val ovrig =
            Ovrig(
                linjeId = 1,
                vedtaksId = "b123",
                henvisning = "c321",
                soknadsType = "NN",
            )

        every { oppdragsInfoService.getOppdragsLinjeOvriger(any(), any()) } returns listOf(ovrig)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/ovrig")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<Ovrig>("linjeId").first().shouldBe(1)
        response.body.jsonPath().getList<Ovrig>("henvisning").first().shouldBe("c321")
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
