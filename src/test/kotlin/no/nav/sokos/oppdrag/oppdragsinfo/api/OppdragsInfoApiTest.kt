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
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.routing.routing
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.OPPDRAGSINFO_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.common.model.GjelderIdRequest
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragDTO
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.dto.OppdragsLinjeDetaljerDTO
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEgenskaper
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsinfoDTO
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsEgenskaperRequest
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta
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

    test("sok oppdragsinfo med gyldig gjelderId skal returnere 200 OK") {
        val oppdragsegenskaper =
            OppdragsEgenskaper(
                fagsystemId = "12345678901",
                oppdragsId = 1234556,
                navnFagGruppe = "faggruppeNavn",
                navnFagOmraade = "fagomraadeNavn",
                kjorIdag = "kjorIdag",
                typeBilag = "bilagsType",
                kodeStatus = "PASS",
            )

        val oppdragsinfoTreffliste =
            OppdragsinfoDTO(
                gjelderId = "12345678901",
                oppdragsListe = listOf(oppdragsegenskaper),
            )

        every { oppdragsInfoService.hentOppdragsEgenskaperList(any(), any(), any()) } returns listOf(oppdragsinfoTreffliste)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(OppdragsEgenskaperRequest(gjelderId = "12345678901", fagGruppeKode = "ABC"))
                .port(PORT)
                .post("$OPPDRAGSINFO_BASE_API_PATH/oppdragsinfo")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<OppdragsinfoDTO>("gjelderId").first().shouldBe("12345678901")
        response.body.jsonPath().getList<OppdragsEgenskaper>("oppdragsListe").shouldHaveSize(1)
    }

    test("sok oppdragsinfo med ugyldig gjelderId skal returnere 400 Bad Request") {

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(OppdragsEgenskaperRequest(gjelderId = "123", fagGruppeKode = ""))
            .port(PORT)
            .post("$OPPDRAGSINFO_BASE_API_PATH/oppdragsinfo")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("gjelderId er ugyldig. Tillatt format er 9 eller 11 siffer"))
    }

    test("hent oppdrag med gyldig gjelderId skal returnere 200 OK") {

        val oppdragDTO =
            OppdragDTO(
                oppdragsegenskaper =
                    OppdragsEgenskaper(
                        fagsystemId = "12345678901",
                        oppdragsId = 1234556,
                        navnFagGruppe = "faggruppeNavn",
                        navnFagOmraade = "fagomraadeNavn",
                        kjorIdag = "kjorIdag",
                        typeBilag = "bilagsType",
                        kodeStatus = "PASS",
                    ),
                kostnadssted =
                    OppdragsEnhet(
                        type = "BOS",
                        datoFom = "2024-01-01",
                        enhet = "0502",
                    ),
                ansvarssted = null,
                omposteringer = TRUE,
                oppdragsLinjer =
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
            )

        every { oppdragsInfoService.hentOppdragslinjer(any(), any()) } returns oppdragDTO

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(GjelderIdRequest(gjelderId = "12345678901"))
                .port(PORT)
                .post("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().get<Boolean>("harOmposteringer").shouldBe(TRUE)
        response.body.jsonPath().get<String>("kostnadssted.datoFom").shouldBe("2024-01-01")
        response.body.jsonPath().getList<OppdragsLinje>("oppdragsLinjer").shouldHaveSize(1)
    }

    test("hent oppdrag med som ikke tilhører gjelderId skal kaste RequestValidationException med 400 Bad Request") {

        every {
            oppdragsInfoService.hentOppdragslinjer(
                any(), any(),
            )
        } throws
            RequestValidationException(
                HttpStatusCode.BadRequest.value, listOf("Oppdraget tilhører ikke gjelderId"),
            )

        RestAssured.given().filter(validationFilter)
            .header(HttpHeaders.ContentType, APPLICATION_JSON)
            .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            .body(GjelderIdRequest(gjelderId = "123456789"))
            .port(PORT)
            .post("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID")
            .then().assertThat()
            .statusCode(HttpStatusCode.BadRequest.value)
            .body("message", equalTo("Oppdraget tilhører ikke gjelderId"))
    }

    test("hent alle faggrupper skal returnere 200 OK") {

        val fagGruppe =
            FagGruppe(
                navn = "ABC",
                type = "DEF",
            )

        every { oppdragsInfoService.hentFaggrupper() } returns listOf(fagGruppe)

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

    test("hent omposteringer for en oppdragsId skal returnere 200 OK") {

        val ompostering =
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
            )

        every { oppdragsInfoService.hentOppdragsOmposteringer(any(), any()) } returns listOf(ompostering)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .body(GjelderIdRequest(gjelderId = "12345678901")).port(PORT)
                .post("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/omposteringer")
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

        every { oppdragsInfoService.hentOppdragsEnhetsHistorikk(any()) } returns listOf(oppdragsEnhet)

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

        val oppdragStatus =
            OppdragStatus(
                kodeStatus = "AKTIV",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.hentOppdragsStatusHistorikk(any()) } returns listOf(oppdragStatus)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/statushistorikk")
                .then().assertThat()
                .statusCode(HttpStatusCode.OK.value)
                .extract().response()

        response.body.jsonPath().getList<OppdragStatus>("kodeStatus").first().shouldBe("AKTIV")
        response.body.jsonPath().getList<OppdragStatus>("tidspktReg").first().shouldBe("2024-01-01")
    }

    test("hent linjestatus i kombinasjon med oppdragsId og linjeId skal returnere 200 OK") {

        val linjeStatus =
            LinjeStatus(
                status = "AKTIV",
                datoFom = "2024-01-01",
                tidspktReg = "2024-01-01",
                brukerid = "A12345",
            )

        every { oppdragsInfoService.hentOppdragsLinjeStatuser(any(), any()) } returns listOf(linjeStatus)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/status")
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

        every { oppdragsInfoService.hentOppdragsLinjeAttestanter(any(), any()) } returns listOf(attestant)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/attestant")
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

        every { oppdragsInfoService.hentOppdragsLinjeDetaljer(any(), any()) } returns oppdragsLinjeDetaljerDTO

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

        every { oppdragsInfoService.hentOppdragsLinjeValutaer(any(), any()) } returns listOf(valuta)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/valuta")
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

        every { oppdragsInfoService.hentOppdragsLinjeSkyldnere(any(), any()) } returns listOf(skyldner)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/skyldner")
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

        every { oppdragsInfoService.hentOppdragsLinjeKravhavere(any(), any()) } returns listOf(kravhaver)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kravhaver")
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

        every { oppdragsInfoService.hentOppdragsLinjeEnheter(any(), any()) } returns listOf(linjeEnhet)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/enhet")
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

        every { oppdragsInfoService.hentOppdragsLinjeGrader(any(), any()) } returns listOf(grad)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/grad")
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

        every { oppdragsInfoService.hentOppdragsLinjeTekster(any(), any()) } returns listOf(tekst)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/tekst")
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

        every { oppdragsInfoService.hentOppdragsLinjeKidListe(any(), any()) } returns listOf(kid)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/kidliste")
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

        every { oppdragsInfoService.hentOppdragsLinjeMaksdatoer(any(), any()) } returns listOf(maksdato)

        val response =
            RestAssured.given().filter(validationFilter)
                .header(HttpHeaders.ContentType, APPLICATION_JSON)
                .header(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
                .port(PORT)
                .get("$OPPDRAGSINFO_BASE_API_PATH/$OPPDRAGS_ID/$LINJE_ID/maksdato")
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

        every { oppdragsInfoService.hentOppdragsLinjeOvriger(any(), any()) } returns listOf(ovrig)

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
