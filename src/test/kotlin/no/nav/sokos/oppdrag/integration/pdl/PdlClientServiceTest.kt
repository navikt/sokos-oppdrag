package no.nav.sokos.oppdrag.integration.pdl

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.pdl.PdlException
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import org.junit.jupiter.api.assertThrows

private const val FNR = "12345678912"

@Ignored
internal class PdlClientServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val pdlClientService: PdlClientService by lazy {
        PdlClientService(
            pdlUrl = wiremock.baseUrl(),
            accessTokenClient = WiremockListener.accessTokenClient,
        )
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

        val response = pdlClientService.getPerson(listOf(FNR))

        response[FNR]?.navn?.first()?.fornavn shouldBe "Ola"
        response[FNR]?.navn?.first()?.mellomnavn shouldBe null
        response[FNR]?.navn?.first()?.etternavn shouldBe "Nordmann"
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
                pdlClientService.getPerson(listOf(FNR))
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
                pdlClientService.getPerson(listOf(FNR))
            }

        exception.message shouldBe "(Path: [\"hentPerson\"], Code: [\"unauthenticated\"], Message: Ikke autentisert)"
    }
})

private val jsonResponseNavnFunnet =
    """
     {
      "data": {
        "hentPersonBolk": [
          {
            "ident": "70078749472",
            "person": {
              "navn": [
                {
                  "fornavn": "TRIVIELL",
                  "mellomnavn": null,
                  "etternavn": "SKILPADDE"
                }
              ],
              "adressebeskyttelse": []
            },
            "code": "ok"
          }
        ]
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
