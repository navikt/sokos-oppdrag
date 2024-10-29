package no.nav.sokos.oppdrag.integration.service

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetClientService
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock

private const val FNR = "12345678912"
private const val SKJERMETFNR = "98765432100"

internal class SkjermetClientServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val skjermetClientService: SkjermetClientService by lazy {
        SkjermetClientService(
            skjermetUrl = wiremock.baseUrl(),
            accessTokenClient = WiremockListener.accessTokenClient,
        )
    }

    test("sjekk om person er skjermet") {
        wiremock.stubFor(
            post(urlEqualTo("/skjermetBulk"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                        .withStatus(HttpStatusCode.OK.value)
                        .withBody(jsonResponseIkkeSkjermet),
                ),
        )

        val response = skjermetClientService.isSkjermedePersonerInSkjermingslosningen(listOf(FNR, SKJERMETFNR))

        response[FNR] shouldBe false
        response[SKJERMETFNR] shouldBe true
    }
})

private val jsonResponseIkkeSkjermet =
    """
    {
        "12345678912": false,
        "98765432100": true
    }
    """.trimIndent()
