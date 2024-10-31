package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.exception.OppdragsinfoException
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository
import org.junit.jupiter.api.assertThrows

private val oppdragsInfoRepository = mockk<OppdragsInfoRepository>()
private val skjermingService = mockk<SkjermingService>()
private val oppdragsInfoService = OppdragsInfoService(oppdragsInfoRepository, skjermingService)

internal class OppdragsInfoServiceTest : FunSpec({

    test("hent liste av oppdragsegenskaper hvis oppdragId finnes og saksbehandler har tilgang til personen") {

        val oppdragList =
            listOf(
                Oppdrag(
                    fagSystemId = "12345678901",
                    oppdragsId = 1234567890,
                    navnFagGruppe = "NAV Arbeid og ytelser",
                    navnFagOmraade = "Arbeidsavklaringspenger",
                    kjorIdag = "J",
                    typeBilag = "B",
                    kodeStatus = "A",
                ),
            )

        coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns false
        every { oppdragsInfoRepository.getOppdragId(any()) } returns "1234567890"
        every { oppdragsInfoRepository.getOppdrag(any(), "") } returns oppdragList

        val result = oppdragsInfoService.getOppdrag("12345678901", "", navIdent)

        result shouldBe oppdragList
    }

    test("hent liste av oppdragsegenskaper kaster exception hvis oppdragId finnes og saksbehandler ikke har tilgang til personen") {

        val oppdragList =
            listOf(
                Oppdrag(
                    fagSystemId = "12345678901",
                    oppdragsId = 1234567890,
                    navnFagGruppe = "NAV Arbeid og ytelser",
                    navnFagOmraade = "Arbeidsavklaringspenger",
                    kjorIdag = "J",
                    typeBilag = "B",
                    kodeStatus = "A",
                ),
            )

        coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns true
        every { oppdragsInfoRepository.getOppdragId(any()) } returns "1234567890"
        every { oppdragsInfoRepository.getOppdrag(any(), "") } returns oppdragList

        assertThrows<OppdragsinfoException> {
            oppdragsInfoService.getOppdrag("12345678901", "", navIdent)
        }
    }

    test("hent liste av oppdragsegenskaper hvis oppdragId ikke finnes") {

        every { oppdragsInfoRepository.getOppdragId(any()) } returns null

        coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns false
        val result = oppdragsInfoService.getOppdrag("12345678901", "", navIdent)

        result.shouldBeEmpty()
    }
})
