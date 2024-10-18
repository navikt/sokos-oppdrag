package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository

private val oppdragsInfoRepository = mockk<OppdragsInfoRepository>()
private val oppdragsInfoService = OppdragsInfoService(oppdragsInfoRepository)

internal class OppdragsInfoServiceTest : FunSpec({

    test("hent liste av oppdragsegenskaper hvis oppdragId finnes") {

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

        every { oppdragsInfoRepository.getOppdragId(any()) } returns "1234567890"
        every { oppdragsInfoRepository.getOppdrag(any(), "") } returns oppdragList

        val result = oppdragsInfoService.getOppdrag("12345678901", "", navIdent)

        result shouldBe oppdragList
    }

    test("hent liste av oppdragsegenskaper hvis oppdragId ikke finnes") {

        every { oppdragsInfoRepository.getOppdragId(any()) } returns null

        val result = oppdragsInfoService.getOppdrag("12345678901", "", navIdent)

        result.shouldBeEmpty()
    }
})
