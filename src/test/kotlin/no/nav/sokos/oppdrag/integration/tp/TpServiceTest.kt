package no.nav.sokos.oppdrag.integration.tp

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.sokos.oppdrag.integration.client.tp.TpClientService
import no.nav.sokos.oppdrag.integration.client.tp.TpException
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock
import org.junit.jupiter.api.assertThrows

private const val TSS_ID = "12345678912"

private val tpClientService =
    TpClientService(
        tpUrl = wiremock.baseUrl(),
    )

internal class TpServiceTest : FunSpec({

    extensions(listOf(WiremockListener))

    val tpClientService: TpClientService by lazy {
        TpClientService(
            tpUrl = wiremock.baseUrl(),
        )
    }

    test("hent leverandørnavn") {
        wiremock.stubFor(
            get(urlEqualTo("/api/ordninger/tss/$TSS_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.ContentType, "application/text;charset=UTF-8")
                        .withBody("Ola Nordmann"),
                ),
        )

        val response = tpClientService.getLeverandorNavn(TSS_ID)
        response.navn shouldBe "Ola Nordmann"
    }

    test("hent leverandørnavn returnerer 404 NotFound") {

        wiremock.stubFor(
            get(urlEqualTo("/api/ordninger/tss/$TSS_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(404),
                ),
        )

        val exception =
            assertThrows<TpException> {
                tpClientService.getLeverandorNavn(TSS_ID)
            }

        exception.shouldNotBeNull()
        exception.apiError.error shouldBe HttpStatusCode.NotFound.description
        exception.apiError.status shouldBe HttpStatusCode.NotFound.value
        exception.apiError.message shouldBe "Fant ingen leverandørnavn med tssId $TSS_ID"
        exception.apiError.path shouldBe "${wiremock.baseUrl()}/api/ordninger/tss/$TSS_ID"
    }
})
