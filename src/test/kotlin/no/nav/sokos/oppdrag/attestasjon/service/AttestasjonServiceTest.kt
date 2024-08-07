package no.nav.sokos.oppdrag.attestasjon.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository

private val applicationCall = mockk<ApplicationCall>()
private val attestasjonRepository = mockk<AttestasjonRepository>()
private val attestasjonService = AttestasjonService(attestasjonRepository)

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
})
