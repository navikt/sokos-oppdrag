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
        every { attestasjonRepository.sok(any(), any(), any(), any(), any()) } returns emptyList()
    }

    context("skal ikke gi valideringsfeil") {
        test("gjelderId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    gjelderId = "12345678901",
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, ikke attesterte") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFaggruppe = "FAGGRUPPE",
                    attestert = false,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, ikke attesterte") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFagomraade = "FAGOMRAADE",
                    attestert = false,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, fagsystemId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    fagsystemId = "fagsystemId",
                    kodeFagomraade = "FAGOMRAADE",
                    applicationCall = applicationCall,
                )
            }
        }
        test("alle parametre") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    gjelderId = "123456789",
                    kodeFaggruppe = "faggruppe",
                    kodeFagomraade = "kodeFagomraade",
                    fagsystemId = "fagsystemId",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagsystemId, uten fagområde, men med gjelderId") {
            shouldNotThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
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
                attestasjonService.hentOppdragForAttestering(applicationCall = applicationCall)
            }
        }
        test("fagområde, attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFagomraade = "FAGOMRAADE",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagområde, både attesterte og ikke attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFagomraade = "FAGOMRAADE",
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFaggruppe = "FAGGRUPPE",
                    attestert = true,
                    applicationCall = applicationCall,
                )
            }
        }
        test("faggruppe, både attesterte og ikke attesterte") {
            shouldThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    kodeFaggruppe = "FAGGRUPPE",
                    attestert = null,
                    applicationCall = applicationCall,
                )
            }
        }
        test("fagsystemId uten fagområde") {
            shouldThrow<RequestValidationException> {
                attestasjonService.hentOppdragForAttestering(
                    fagsystemId = "fagsystemId",
                    kodeFaggruppe = "FAGGRUPPE",
                    attestert = null,
                    applicationCall = applicationCall,
                )
            }
        }
    }
})
