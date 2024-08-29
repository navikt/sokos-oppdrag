package no.nav.sokos.oppdrag.attestasjon.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponse
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSKlient

private val applicationCall = mockk<ApplicationCall>()
private val attestasjonRepository = mockk<AttestasjonRepository>()
private val zosKlient: ZOSKlient = mockk<ZOSKlient>()
private val attestasjonService = AttestasjonService(attestasjonRepository, zosKlient = zosKlient)

internal class AttestasjonServiceTest : FunSpec({

    beforeTest {
        every { applicationCall.request.headers["Authorization"] } returns tokenWithNavIdent
        every { attestasjonRepository.getOppdrag(any(), any(), any(), any(), any()) } returns emptyList()
    }

    context("skal ikke gi valideringsfeil") {
        test("gjelderId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    gjelderId = "12345678901",
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, ikke attesterte") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagGruppe = "FAGGRUPPE",
                    attestert = false,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, ikke attesterte") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagOmraade = "FAGOMRAADE",
                    attestert = false,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, fagsystemId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    fagsystemId = "fagsystemId",
                    kodeFagOmraade = "FAGOMRAADE",
                    applicationCall = applicationCall,
                )
            }
        }
        test("alle parametre") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    gjelderId = "123456789",
                    kodeFagGruppe = "faggruppe",
                    kodeFagOmraade = "kodeFagomraade",
                    fagsystemId = "fagsystemId",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagsystemId, uten fagområde, men med gjelderId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    gjelderId = "123456789",
                    fagsystemId = "fagsystemId",
                    applicationCall = applicationCall,
                )
            }
        }
    }

    context("Skal gi valideringsfeil") {
        test("Ingen søkeparametre") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(applicationCall = applicationCall)
            }
        }
        test("fagområde, attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagOmraade = "FAGOMRAADE",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, både attesterte og ikke attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagOmraade = "FAGOMRAADE",
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagGruppe = "FAGGRUPPE",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, både attesterte og ikke attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    kodeFagGruppe = "FAGGRUPPE",
                    attestert = null,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagsystemId uten fagområde") {
            shouldThrow<RequestValidationException> {
                attestasjonService.getOppdrag(
                    fagsystemId = "fagsystemId",
                    kodeFagGruppe = "FAGGRUPPE",
                    attestert = null,
                    applicationCall = applicationCall,
                )
            }
        }
    }

    test("attestasjon av oppdrag") {
        val oppdragsid = 999999999

        val request =
            AttestasjonRequest(
                gjelderId = "string",
                fagOmraade = "string",
                oppdragsId = oppdragsid,
                brukerId = "string",
                kjorIdag = false,
                linjer =
                    listOf(
                        AttestasjonLinje(
                            linjeId = 99999,
                            attestantId = "string",
                            datoUgyldigFom = "string",
                        ),
                    ),
            )

        val response =
            PostOSAttestasjonResponse200(
                osAttestasjonOperationResponse =
                    PostOSAttestasjonResponse200OSAttestasjonOperationResponse(
                        attestasjonskvittering =
                            PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering(
                                responsAttestasjon =
                                    PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon(
                                        gjelderId = "string",
                                        oppdragsId = 999999999,
                                        antLinjerMottatt = 99999,
                                        statuskode = 99,
                                        melding = "string",
                                    ),
                            ),
                    ),
            )

        coEvery { zosKlient.attestereOppdrag(any()) } returns response
        attestasjonService.attestereOppdrag(request) shouldBe response
    }
})
