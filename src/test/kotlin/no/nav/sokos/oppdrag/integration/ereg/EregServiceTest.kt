package no.nav.sokos.oppdrag.integration.ereg

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.integration.client.ereg.EregClientService
import no.nav.sokos.oppdrag.integration.client.ereg.EregException
import no.nav.sokos.oppdrag.integration.client.ereg.Navn
import no.nav.sokos.oppdrag.integration.client.ereg.Organisasjon
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import org.junit.jupiter.api.assertThrows

private const val GYLDIG_ORGANISASJONSNUMMER = "123456789"
private const val UGYLDIG_ORGANISASJONSNUMMER = "12345678"
private const val IKKE_FUNNET_ORGANISASJONSNUMMER = "821230153"

internal class EregServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val eregClientService: EregClientService by lazy {
        EregClientService(
            eregUrl = wiremock.baseUrl(),
        )
    }

    test("hent organisasjonsnavn") {
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$GYLDIG_ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    okJson(jsonResponseOrgFunnet),
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
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$UGYLDIG_ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withBody(jsonResponseOrgBadRequest),
                ),
        )

        val exception =
            assertThrows<EregException> {
                eregClientService.getOrganisasjonsNavn(UGYLDIG_ORGANISASJONSNUMMER)
            }

        exception.shouldNotBeNull()
        exception.apiError.error shouldBe HttpStatusCode.BadRequest.description
        exception.apiError.status shouldBe HttpStatusCode.BadRequest.value
        exception.apiError.message shouldBe "Organisasjonsnummeret (${UGYLDIG_ORGANISASJONSNUMMER}) er på et ugyldig format"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$UGYLDIG_ORGANISASJONSNUMMER/noekkelinfo"
    }

    test("hent organisasjonsnavn returnerer 404 NotFound") {
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$IKKE_FUNNET_ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withBody(jsonResponseOrgIkkeFunnet),
                ),
        )

        val exception =
            assertThrows<EregException> {
                eregClientService.getOrganisasjonsNavn(IKKE_FUNNET_ORGANISASJONSNUMMER)
            }

        exception.shouldNotBeNull()
        exception.apiError.status shouldBe HttpStatusCode.NotFound.value
        exception.apiError.error shouldBe HttpStatusCode.NotFound.description
        exception.apiError.message shouldBe "Ingen organisasjon med organisasjonsnummer $IKKE_FUNNET_ORGANISASJONSNUMMER ble funnet"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$IKKE_FUNNET_ORGANISASJONSNUMMER/noekkelinfo"
    }
})

private val jsonResponseOrgFunnet =
    """
    {
      "navn": {
       "sammensattnavn": "NAV AS"
      }
    }
    """.trimIndent()

private val jsonResponseOrgBadRequest =
    """
    {
      "melding": "Organisasjonsnummeret (${UGYLDIG_ORGANISASJONSNUMMER}) er på et ugyldig format"
    }
    """.trimIndent()

private val jsonResponseOrgIkkeFunnet =
    """
    {
      "melding": "Ingen organisasjon med organisasjonsnummer $IKKE_FUNNET_ORGANISASJONSNUMMER ble funnet"
    }
    """.trimIndent()
