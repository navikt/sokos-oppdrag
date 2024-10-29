package no.nav.sokos.oppdrag.integration.ereg

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import org.junit.jupiter.api.assertThrows

const val ORGANISASJONSNUMMER = "123456789"

internal class EregServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val eregClientService: EregClientService by lazy {
        EregClientService(
            eregUrl = wiremock.baseUrl(),
        )
    }

    test("hent organisasjonsnavn") {
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    okJson(jsonResponseOrgFunnet),
                ),
        )

        val response = eregClientService.getOrganisasjonsNavn(ORGANISASJONSNUMMER)
        response shouldBe
            Organisasjon(
                Navn(
                    sammensattnavn = "NAV AS",
                ),
            )
    }

    test("hent organisasjonsnavn returnerer 400 BadRequest") {
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withBody(jsonResponseOrgBadRequest),
                ),
        )

        val exception =
            assertThrows<EregException> {
                eregClientService.getOrganisasjonsNavn(ORGANISASJONSNUMMER)
            }

        exception.shouldNotBeNull()
        exception.apiError.error shouldBe "Bad Request"
        exception.apiError.status shouldBe 400
        exception.apiError.message shouldBe "Organisasjonsnummeret (12) er på et ugyldig format"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$ORGANISASJONSNUMMER/noekkelinfo"
    }

    test("hent organisasjonsnavn returnerer 404 NotFound") {
        wiremock.stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGANISASJONSNUMMER/noekkelinfo"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withBody(jsonResponseOrgIkkeFunnet),
                ),
        )

        val exception =
            assertThrows<EregException> {
                eregClientService.getOrganisasjonsNavn(ORGANISASJONSNUMMER)
            }

        exception.shouldNotBeNull()
        exception.apiError.error shouldBe "Not Found"
        exception.apiError.status shouldBe 404
        exception.apiError.message shouldBe "Ingen organisasjon med organisasjonsnummer $ORGANISASJONSNUMMER ble funnet"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/v2/organisasjon/$ORGANISASJONSNUMMER/noekkelinfo"
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
      "melding": "Organisasjonsnummeret (12) er på et ugyldig format"
    }
    """.trimIndent()

private val jsonResponseOrgIkkeFunnet =
    """
    {
      "melding": "Ingen organisasjon med organisasjonsnummer $ORGANISASJONSNUMMER ble funnet"
    }
    """.trimIndent()
