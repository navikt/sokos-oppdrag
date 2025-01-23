package no.nav.sokos.oppdrag.fastedata.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk

import no.nav.sokos.oppdrag.fastedata.korrigeringsaarsakDTOs
import no.nav.sokos.oppdrag.fastedata.korrigeringsaarsaker
import no.nav.sokos.oppdrag.fastedata.repository.FagomraadeRepository

internal class FasteDataServiceTest :
    FunSpec({

        val fagomraadeRepository = mockk<FagomraadeRepository>()

        val fasteDataService: FasteDataService by lazy {
            FasteDataService(fagomraadeRepository)
        }

        afterEach {
            clearAllMocks()
        }

        test("getKorrigeringsaarsaker for any fagomraade return list of korrigeringsaarsaker from repository") {
            coEvery { fagomraadeRepository.getKorrigeringsaarsaker(any()) } returns korrigeringsaarsaker

            fasteDataService.getKorrigeringsaarsaker("PEN") shouldBe korrigeringsaarsakDTOs
        }
    })
