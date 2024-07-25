package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEgenskaper
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsinfoDTO
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsInfoRepository

private val applicationCall = mockk<ApplicationCall>()
private val oppdragsInfoRepository = mockk<OppdragsInfoRepository>()
private val oppdragsInfoService = OppdragsInfoService(oppdragsInfoRepository)

internal class OppdragsInfoServiceTest : FunSpec({

    test("test sokOppdrag") {
        val oppdragsegenskaper =
            OppdragsEgenskaper(
                fagsystemId = "12345678901",
                oppdragsId = 1234567890,
                navnFagGruppe = "NAV Arbeid og ytelser",
                navnFagOmraade = "Arbeidsavklaringspenger",
                kjorIdag = "J",
                typeBilag = "B",
                kodeStatus = "A",
            )

        val oppdragsinfoTreffliste =
            OppdragsinfoDTO(
                gjelderId = "12345678901",
            )

        every { applicationCall.request.headers["Authorization"] } returns tokenWithNavIdent
        every { oppdragsInfoRepository.hentOppdragsInfo("12345678901") } returns oppdragsinfoTreffliste
        every { oppdragsInfoRepository.hentOppdragsegenskaperList("12345678901", "") } returns listOf(oppdragsegenskaper)

        val result = oppdragsInfoService.hentOppdragsEgenskaperList("12345678901", "", applicationCall)

        result.first().gjelderId shouldBe "12345678901"
        result.first().oppdragsListe?.shouldHaveSize(1)
    }

    test("test hentOppdrag") {

        val oppdragsId = 12345
        val gjelderId = "12345678901"

        val oppdragsegenskaper =
            OppdragsEgenskaper(
                fagsystemId = "123456789",
                oppdragsId = oppdragsId,
                navnFagGruppe = "faggruppeNavn",
                navnFagOmraade = "fagomraadeNavn",
                kjorIdag = "kjorIdag",
                typeBilag = "bilagsType",
                kodeStatus = "PASS",
            )

        val oppdragsenhet =
            OppdragsEnhet(
                type = "BOS",
                datoFom = "2024-01-01",
                enhet = "0502",
            )
        val behandlendeOppdragsenhet =
            OppdragsEnhet(
                type = "BEH",
                datoFom = "2024-01-01",
                enhet = "0101",
            )

        val oppdragsLinje =
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
            )

        every { oppdragsInfoRepository.erOppdragTilknyttetBruker(gjelderId, oppdragsId) } returns true
        every { oppdragsInfoRepository.hentOppdragsEnhet(oppdragsId = oppdragsId) } returns listOf(oppdragsenhet)
        every { oppdragsInfoRepository.hentOppdragsEnhet(typeEnhet = "BEH", oppdragsId = oppdragsId) } returns
            listOf(
                behandlendeOppdragsenhet,
            )
        every { oppdragsInfoRepository.eksistererOmposteringer(gjelderId, oppdragsId) } returns true
        every { oppdragsInfoRepository.hentOppdragsLinjer(oppdragsId) } returns listOf(oppdragsLinje)
        every { oppdragsInfoRepository.hentOppdragsegenskaper(oppdragsId) } returns listOf(oppdragsegenskaper)

        val result = oppdragsInfoService.hentOppdragslinjer(gjelderId, oppdragsId)

        result.kostnadssted.enhet shouldBe "0502"
        result.ansvarssted?.enhet shouldBe "0101"
        result.omposteringer shouldBe true
        result.oppdragsLinjer.shouldHaveSize(1)
        result.oppdragsLinjer.first().kodeKlasse shouldBe "ABC"
    }
})
