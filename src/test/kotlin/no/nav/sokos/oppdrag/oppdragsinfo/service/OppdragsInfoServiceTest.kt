package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository

private val applicationCall = mockk<ApplicationCall>()
private val oppdragsInfoRepository = mockk<OppdragsInfoRepository>()
private val oppdragsInfoService = OppdragsInfoService(oppdragsInfoRepository)

internal class OppdragsInfoServiceTest : FunSpec({

    test("hent liste av oppdragsegenskaper") {
        val oppdragList =
            listOf(
                Oppdrag(
                    fagsystemId = "12345678901",
                    oppdragsId = 1234567890,
                    navnFagGruppe = "NAV Arbeid og ytelser",
                    navnFagomraade = "Arbeidsavklaringspenger",
                    kjorIdag = "J",
                    typeBilag = "B",
                    kodeStatus = "A",
                ),
            )

        every { applicationCall.request.headers["Authorization"] } returns tokenWithNavIdent
        every { oppdragsInfoRepository.getOppdragId(any()) } returns "1234567890"
        every { oppdragsInfoRepository.getOppdrag(any(), "") } returns oppdragList

        val result = oppdragsInfoService.getOppdrag("12345678901", "", applicationCall)

        result.size shouldBe 1
    }

    test("hent liste av oppdragslinjer") {

        val oppdragsId = 12345
        val gjelderId = "12345678901"

        val oppdragsLinjeList =
            listOf(
                OppdragsLinje(
                    linjeId = 11,
                    kodeKlasse = "ABC",
                    datoVedtakFom = "2024-01-01",
                    datoVedtakTom = null,
                    sats = 99.9,
                    typeSats = "DAG",
                    kodeStatus = "X",
                    datoFom = "2024-01-01",
                    linjeIdKorr = 22,
                    attestert = "J",
                    delytelseId = "D3",
                    utbetalesTilId = "A1B2",
                    refunderesOrgnr = "123456789",
                    brukerId = "abc123",
                    tidspktReg = "2024-01-01",
                ),
            )

        every { oppdragsInfoRepository.getOppdragsLinjer(any()) } returns oppdragsLinjeList

        val result = oppdragsInfoService.getOppdragsLinjer(oppdragsId)

        result.size shouldBe 1
    }
})
