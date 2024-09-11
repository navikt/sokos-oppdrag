package no.nav.sokos.oppdrag.integration.pdl

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import no.nav.sokos.oppdrag.security.AccessTokenClient
import org.junit.jupiter.api.assertThrows

private val accessTokenClient = mockk<AccessTokenClient>()
private val pdlService =
    PdlService(
        pdlUrl = wiremock.baseUrl(),
        accessTokenClient = accessTokenClient,
    )

internal class PdlServiceTest : FunSpec({

    beforeTest {
        coEvery { accessTokenClient.getSystemToken() } returns "token"
    }

    test("hent navn fra PDL returnerer 200 OK") {
        wiremock.stubFor(
            post(urlEqualTo("/graphql"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.OK.value)
                        .withBody(jsonResponseNavnFunnet),
                ),
        )

        val response = pdlService.getPersonNavn("12345678912")

        response?.navn?.first()?.fornavn shouldBe "Ola"
        response?.navn?.first()?.mellomnavn shouldBe null
        response?.navn?.first()?.etternavn shouldBe "Nordmann"
    }

    test("hent navn fra pdl returnerer json svar at person ikke finnes") {
        wiremock.stubFor(
            post(urlEqualTo("/graphql"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.OK.value)
                        .withBody(jsonResponseNavnIkkeFunnet),
                ),
        )

        val exception =
            assertThrows<PdlException> {
                pdlService.getPersonNavn("12345678912")
            }

        exception.message shouldBe "(Path: [\"hentPerson\"], Code: [\"not_found\"], Message: Fant ikke person)"
    }

    test("hent navn fra pdl returnerer json svar at clienten ikke er autentisert") {
        wiremock.stubFor(
            post(urlEqualTo("/graphql"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.OK.value)
                        .withBody(jsonResponseIkkeAutentisert),
                ),
        )

        val exception =
            assertThrows<PdlException> {
                pdlService.getPersonNavn("12345678912")
            }

        exception.message shouldBe "(Path: [\"hentPerson\"], Code: [\"unauthenticated\"], Message: Ikke autentisert)"
    }
})

private val jsonResponseNavnFunnet =
    """
    {
      "data": {
        "hentPerson": {
          "navn": [
            {
              "fornavn": "Ola",
              "mellomnavn": null,
              "etternavn": "Nordmann"
            }
          ]
        }
      }
    }
    """.trimIndent()

private val jsonResponseNavnIkkeFunnet =
    """
    {
      "errors": [
        {
          "message": "Fant ikke person",
          "locations": [
            {
              "line": 2,
              "column": 3
            }
          ],
          "path": [
            "hentPerson"
          ],
          "extensions": {
            "code": "not_found",
            "classification": "ExecutionAborted"
          }
        }
      ],
      "data": {
        "hentPerson": null
      }
    }
    """.trimIndent()

private val jsonResponseIkkeAutentisert =
    """
    {
      "errors": [
        {
          "message": "Ikke autentisert",
          "locations": [
            {
              "line": 2,
              "column": 2
            }
          ],
          "path": [
            "hentPerson"
          ],
          "extensions": {
            "code": "unauthenticated",
            "classification": "ExecutionAborted"
          }
        }
      ],
      "data": {
        "hentPerson": null
      }
    }
    """.trimIndent()
