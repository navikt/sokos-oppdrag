package no.nav.sokos.oppdrag.integration.service

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetClientService
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetException
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import org.junit.jupiter.api.assertThrows

private const val FNR = "12345678912"
private const val FNR_SKJERMET = "98765432100"

internal class SkjermetClientServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val skjermetClientService: SkjermetClientService by lazy {
        SkjermetClientService(
            skjermetUrl = wiremock.baseUrl(),
            accessTokenClient = WiremockListener.accessTokenClient,
        )
    }

    test("hent skjermet personer returnerer 200 OK") {
        wiremock.stubFor(
            post(urlEqualTo("/skjermetBulk"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.OK.value)
                        .withBody(jsonResponseSkjermetPersoner),
                ),
        )

        val response = skjermetClientService.isSkjermedePersonerInSkjermingslosningen(listOf(FNR, FNR_SKJERMET))

        response[FNR] shouldBe false
        response[FNR_SKJERMET] shouldBe true
    }

    test("hent skjermet personer returnerer 400 BadRequest") {
        wiremock.stubFor(
            post(urlEqualTo("/skjermetBulk"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.BadRequest.value)
                        .withBody(jsonResponseSkjermetPersonerBadRequest),
                ),
        )

        val exception =
            assertThrows<SkjermetException> {
                skjermetClientService.isSkjermedePersonerInSkjermingslosningen(emptyList())
            }

        exception.shouldNotBeNull()
        exception.apiError.status shouldBe HttpStatusCode.BadRequest.value
        exception.apiError.error shouldBe HttpStatusCode.BadRequest.description
        exception.apiError.message shouldBe "Personident mangler"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/skjermetBulk"
    }
})

private val jsonResponseSkjermetPersoner =
    """
    {
        $FNR: false,
        $FNR_SKJERMET": true
    }
    """.trimIndent()

private val jsonResponseSkjermetPersonerBadRequest =
    """
    {
      "message": "Personident mangler"
    }
    """.trimIndent()
