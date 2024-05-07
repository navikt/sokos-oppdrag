package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.pdl.hentperson.Navn
import no.nav.pdl.hentperson.Person
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.sokos.oppdrag.common.config.DatabaseConfig
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.tp.TpService
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererOmposteringer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.erOppdragTilknyttetBruker
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsLinjer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsListe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdrag
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje
import java.sql.Connection

val applicationCall = mockk<ApplicationCall>()
val db2DataSource = mockk<DatabaseConfig>(relaxed = true)
val connection = mockk<Connection>(relaxed = true)
val pdlService = mockk<PdlService>()
val eregService = mockk<EregService>()
val tpService = mockk<TpService>()
val oppdragsInfoService =
    OppdragsInfoService(
        databaseConfig = db2DataSource,
        pdlService = pdlService,
        eregService = eregService,
        tpService = tpService,
    )

internal class OppdragsInfoServiceTest : FunSpec({

    beforeEach {
        mockkObject(OppdragsInfoRepository)
        every { db2DataSource.connection } returns connection
    }

    test("test sokOppdrag") {

        val oppdrag =
            Oppdrag(
                fagsystemId = "12345678901",
                oppdragsId = 1234567890,
                navnFagGruppe = "NAV Arbeid og ytelser",
                navnFagOmraade = "Arbeidsavklaringspenger",
                kjorIdag = "J",
                typeBilag = "B",
                kodeStatus = "A",
            )

        val oppdragsInfo =
            OppdragsInfo(
                gjelderId = "12345678901",
            )

        val person =
            Person(
                listOf(
                    Navn(
                        fornavn = "Ola",
                        mellomnavn = "Mellomnavn",
                        etternavn = "Nordmann",
                    ),
                ),
            )

        every { applicationCall.request.headers["Authorization"] } returns MockOAuth2Server().tokenFromDefaultProvider()
        every { connection.hentOppdragsInfo("12345678901") } returns listOf(oppdragsInfo)
        every { connection.hentOppdragsListe("12345678901", "") } returns listOf(oppdrag)
        every { pdlService.getPersonNavn(any()) } returns person

        val result = oppdragsInfoService.sokOppdrag("12345678901", "", applicationCall)

        result.first().gjelderId shouldBe "12345678901"
        result.first().gjelderNavn shouldBe "Ola Mellomnavn Nordmann"
        result.first().oppdragsListe?.shouldHaveSize(1)
    }

    test("test hentOppdrag") {

        val oppdragsId = 12345
        val gjelderId = "12345678901"

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

        every { connection.erOppdragTilknyttetBruker(gjelderId, oppdragsId) } returns true
        every { connection.hentOppdragsEnhet(oppdragsId = oppdragsId) } returns listOf(oppdragsenhet)
        every { connection.hentOppdragsEnhet(typeEnhet = "BEH", oppdragsId = oppdragsId) } returns
            listOf(
                behandlendeOppdragsenhet,
            )
        every { connection.eksistererOmposteringer(gjelderId, oppdragsId) } returns true
        every { connection.hentOppdragsLinjer(oppdragsId) } returns listOf(oppdragsLinje)

        val result = oppdragsInfoService.hentOppdrag(gjelderId, oppdragsId.toString())

        result.enhet.enhet shouldBe "0502"
        result.behandlendeEnhet?.enhet shouldBe "0101"
        result.harOmposteringer shouldBe true
        result.oppdragsLinjer.shouldHaveSize(1)
        result.oppdragsLinjer.first().kodeKlasse shouldBe "ABC"
    }
})

private fun MockOAuth2Server.tokenFromDefaultProvider() =
    issueToken(
        issuerId = "default",
        clientId = "default",
        tokenCallback =
            DefaultOAuth2TokenCallback(
                claims =
                    mapOf(
                        "NAVident" to "Z123456",
                    ),
            ),
    ).serialize()
