package no.nav.sokos.oppdrag.attestasjon.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
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
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService

private val applicationCall = mockk<ApplicationCall>()
private val attestasjonRepository = mockk<AttestasjonRepository>()
private val zosConnectService: ZOSConnectService = mockk<ZOSConnectService>()
private val attestasjonService = AttestasjonService(attestasjonRepository, zosConnectService = zosConnectService)

internal class AttestasjonServiceTest : FunSpec({

    beforeTest {
        every { applicationCall.request.headers["Authorization"] } returns tokenWithNavIdent
        every { attestasjonRepository.getOppdrag(any(), any(), any(), any(), any()) } returns emptyList()
    }

    test("attestasjon av oppdrag") {
        val oppdragsid = 999999999

        val request =
            AttestasjonRequest(
                gjelderId = "12345678900",
                fagSystemId = "98765432100",
                kodeFagOmraade = "BEH",
                oppdragsId = oppdragsid,
                linjer =
                    listOf(
                        AttestasjonLinje(
                            linjeId = 99999,
                            attestantIdent = "Z999999",
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

        coEvery { zosConnectService.attestereOppdrag(any(), any()) } returns response
        attestasjonService.attestereOppdrag(applicationCall, request) shouldBe response
    }
})
