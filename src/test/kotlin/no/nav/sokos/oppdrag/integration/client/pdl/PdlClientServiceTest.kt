package no.nav.sokos.oppdrag.integration.client.pdl

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock

private const val FNR = "12345678912"
private val FNR_LIST =
    listOf(
        "08527338671",
        "02437024319",
        "30074203236",
    )

internal class PdlClientServiceTest :
    FunSpec({

        extensions(listOf(WiremockListener))

        val pdlClientService: PdlClientService by lazy {
            PdlClientService(
                pdlUrl = wiremock.baseUrl(),
                accessTokenClient = WiremockListener.accessTokenClient,
            )
        }

        test("hent personer fra PDL returnerer personer") {

            val personerFunnetResponse = "pdl/personerFunnetResponse.json".readFromResource()

            wiremock.stubFor(
                post(urlEqualTo("/graphql"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(personerFunnetResponse),
                    ),
            )

            val response = pdlClientService.getPerson(FNR_LIST)

            response.size shouldBe 3
            response["08527338671"]?.navn?.get(0)?.fornavn shouldBe "UEMOSJONELL"
            response["08527338671"]?.navn?.get(0)?.etternavn shouldBe "HØYTTALER"
            response["08527338671"]?.adressebeskyttelse?.get(0)?.gradering shouldBe AdressebeskyttelseGradering.STRENGT_FORTROLIG

            response["02437024319"]?.navn?.get(0)?.fornavn shouldBe "AKROBATISK"
            response["02437024319"]?.navn?.get(0)?.etternavn shouldBe "JURIST"
            response["02437024319"]?.adressebeskyttelse?.get(0)?.gradering shouldBe AdressebeskyttelseGradering.FORTROLIG

            response["30074203236"]?.navn?.get(0)?.fornavn shouldBe "FIN"
            response["30074203236"]?.navn?.get(0)?.etternavn shouldBe "VANE"
            response["30074203236"]?.adressebeskyttelse?.size shouldBe 0
        }

        test("hent personer fra pdl returnerer personer ikke funnet") {

            val personerIkkeFunnetResponse = "pdl/personerIkkeFunnetResponse.json".readFromResource()

            wiremock.stubFor(
                post(urlEqualTo("/graphql"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(personerIkkeFunnetResponse),
                    ),
            )

            val response = pdlClientService.getPerson(listOf(FNR))

            response.size shouldBe 0
        }

        test("hent personer fra pdl returnerer clienten ikke er autentisert") {

            val ikkeAutentisertResponse = "pdl/ikkeAutentisertResponse.json".readFromResource()

            wiremock.stubFor(
                post(urlEqualTo("/graphql"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(ikkeAutentisertResponse),
                    ),
            )

            val exception =
                shouldThrow<PdlException> {
                    pdlClientService.getPerson(listOf(FNR))
                }

            exception.message shouldBe "(Path: [\"hentPersonBolk\"], Code: [\"unauthenticated\"], Message: Ikke autentisert)"
        }

        test("hent personer fra pdl returnerer clienten ikke sender med behandlingsnummer") {

            val manglerBehandlingsnummerResponse = "pdl/manglerBehandlingsnummerResponse.json".readFromResource()

            wiremock.stubFor(
                post(urlEqualTo("/graphql"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(manglerBehandlingsnummerResponse),
                    ),
            )

            val exception =
                shouldThrow<PdlException> {
                    pdlClientService.getPerson(listOf(FNR))
                }

            exception.message shouldBe "(Path: [\"hentPersonBolk\"], Code: [\"bad_request\"], Message: Mangler behandlingsnummer)"
        }
    })
