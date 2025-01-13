package no.nav.sokos.oppdrag.attestasjon.service.zos

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.assertThrows

import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.listener.WiremockListener
import no.nav.sokos.oppdrag.listener.WiremockListener.wiremock

internal class ZOSConnectServiceTest :
    FunSpec({

        extensions(listOf(WiremockListener))

        val zosConnectService: ZOSConnectService by lazy {
            ZOSConnectService(
                zOsUrl = wiremock.baseUrl(),
            )
        }

        test("attesterer oppdrag gir http status kode 200 OK, og statuskode 0 fra OppdragZ") {
            wiremock.stubFor(
                post(urlEqualTo("/oppdaterAttestasjon"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(Json.encodeToString(jsonResponseOppdragslinjeFunnet)),
                    ),
            )

            val response =
                zosConnectService.attestereOppdrag(
                    AttestasjonRequest(
                        "123456789",
                        "fagSystemId",
                        "kodeFagOmraade",
                        1,
                        listOf(AttestasjonLinje(1, "Z999999", "2021-01-01")),
                    ),
                    "Z999999",
                )

            response.message shouldBe "Oppdatering vellykket. 1 linjer oppdatert"
        }

        test("attesterer oppdrag gir http status kode 400 Bad Request, og statuskode 8 fra OppdragZ hvor oppdragslinje ikke funnet") {
            wiremock.stubFor(
                post(urlEqualTo("/oppdaterAttestasjon"))
                    .willReturn(
                        aResponse()
                            .withHeader(HttpHeaders.ContentType, APPLICATION_JSON)
                            .withStatus(HttpStatusCode.OK.value)
                            .withBody(Json.encodeToString(jsonResponseOppdragslinjeIkkeFunnet)),
                    ),
            )

            val exception =
                assertThrows<ZOSException> {
                    zosConnectService.attestereOppdrag(
                        AttestasjonRequest(
                            "123456789",
                            "fagSystemId",
                            "kodeFagOmraade",
                            1,
                            listOf(AttestasjonLinje(1, "Z999999", "2021-01-01")),
                        ),
                        "Z999999",
                    )
                }

            exception.apiError.error shouldBe HttpStatusCode.BadRequest.description
            exception.apiError.status shouldBe HttpStatusCode.BadRequest.value
            exception.apiError.message shouldBe "Oppdragslinje ikke funnet: 43128412345/63835213"
            exception.apiError.path shouldBe "${wiremock.baseUrl()}/oppdaterAttestasjon"
        }
    })

private val jsonResponseOppdragslinjeFunnet =
    PostOSAttestasjonResponse200(
        osAttestasjonOperationResponse =
            PostOSAttestasjonResponse200OSAttestasjonOperationResponse(
                attestasjonskvittering =
                    PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering(
                        responsAttestasjon =
                            PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon(
                                "43128412345",
                                63835213,
                                1,
                                0,
                                "",
                            ),
                    ),
            ),
    )

private val jsonResponseOppdragslinjeIkkeFunnet =
    PostOSAttestasjonResponse200(
        osAttestasjonOperationResponse =
            PostOSAttestasjonResponse200OSAttestasjonOperationResponse(
                attestasjonskvittering =
                    PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering(
                        responsAttestasjon =
                            PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon(
                                "43128412345",
                                63835213,
                                1,
                                8,
                                "Oppdragslinje ikke funnet: 43128412345/63835213",
                            ),
                    ),
            ),
    )
