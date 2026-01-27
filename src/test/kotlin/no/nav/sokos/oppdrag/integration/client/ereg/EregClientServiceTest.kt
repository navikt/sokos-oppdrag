package no.nav.sokos.oppdrag.integration.client.ereg

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.integration.exception.IntegrationException
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock

private const val GYLDIG_ORGANISASJONSNUMMER = "123456789"
private const val UGYLDIG_ORGANISASJONSNUMMER = "12345678"
private const val IKKE_FUNNET_ORGANISASJONSNUMMER = "821230153"

internal class EregClientServiceTest :
    FunSpec({

        extensions(listOf(WiremockListener))

        val eregClientService: EregClientService by lazy {
            EregClientService(
                eregUrl = wiremock.baseUrl(),
            )
        }

        test("hent organisasjonsnavn") {

            val orgFunnetResponse = "ereg/orgFunnetResponse.json".readFromResource()

            wiremock.stubFor(
                get(urlEqualTo("/v2/organisasjon/$GYLDIG_ORGANISASJONSNUMMER/noekkelinfo"))
                    .willReturn(
                        okJson(orgFunnetResponse),
                    ),
            )

            val response = eregClientService.getOrganisasjonsNavn(GYLDIG_ORGANISASJONSNUMMER)
            response shouldBe
                Organisasjon(
                    Navn(
                        sammensattnavn = "NAV AS",
                    ),
                )
        }

        test("hent organisasjonsnavn returnerer 400 BadRequest") {

            val orgUgyldigFormatResponse = "ereg/orgUgyldigFormatResponse.json".readFromResource()

            wiremock.stubFor(
                get(urlEqualTo("/v2/organisasjon/$UGYLDIG_ORGANISASJONSNUMMER/noekkelinfo"))
                    .willReturn(
                        aResponse()
                            .withStatus(400)
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withBody(orgUgyldigFormatResponse),
                    ),
            )

            val exception =
                shouldThrow<IntegrationException> {
                    eregClientService.getOrganisasjonsNavn(UGYLDIG_ORGANISASJONSNUMMER)
                }

            exception.shouldNotBeNull()
            exception.apiError.error shouldBe HttpStatusCode.BadRequest.description
            exception.apiError.status shouldBe HttpStatusCode.BadRequest.value
            exception.apiError.message shouldBe "Organisasjonsnummeret ($UGYLDIG_ORGANISASJONSNUMMER) er på et ugyldig format"
            exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$UGYLDIG_ORGANISASJONSNUMMER/noekkelinfo"
        }

        test("hent organisasjonsnavn returnerer 404 NotFound") {

            val orgIkkeFunnetResponse = "ereg/orgIkkeFunnetResponse.json".readFromResource()

            wiremock.stubFor(
                get(urlEqualTo("/v2/organisasjon/$IKKE_FUNNET_ORGANISASJONSNUMMER/noekkelinfo"))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withBody(orgIkkeFunnetResponse),
                    ),
            )

            val exception =
                shouldThrow<IntegrationException> {
                    eregClientService.getOrganisasjonsNavn(IKKE_FUNNET_ORGANISASJONSNUMMER)
                }

            exception.shouldNotBeNull()
            exception.apiError.status shouldBe HttpStatusCode.NotFound.value
            exception.apiError.error shouldBe HttpStatusCode.NotFound.description
            exception.apiError.message shouldBe "Ingen organisasjon med organisasjonsnummer $IKKE_FUNNET_ORGANISASJONSNUMMER ble funnet"
            exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$IKKE_FUNNET_ORGANISASJONSNUMMER/noekkelinfo"
        }
    })
