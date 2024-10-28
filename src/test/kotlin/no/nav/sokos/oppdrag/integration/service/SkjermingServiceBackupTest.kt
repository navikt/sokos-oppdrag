package no.nav.sokos.oppdrag.integration.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdResponse
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.ereg.Navn
import no.nav.sokos.oppdrag.integration.ereg.Organisasjon
import no.nav.sokos.oppdrag.integration.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.skjerming.SkjermingService
import no.nav.sokos.oppdrag.integration.tp.TpResponse
import no.nav.sokos.oppdrag.integration.tp.TpService

private val eregService = mockk<EregService>()
private val tpService = mockk<TpService>()
private val pdlClientService = mockk<PdlClientService>()
private val skjermetServiceBackup = mockk<SkjermetServiceBackup>()

private val skjermingService = SkjermingService(pdlClientService = pdlClientService, tpService = tpService, eregService = eregService, skjermetServiceBackup = skjermetServiceBackup)

internal class SkjermingServiceBackupTest : FunSpec({

    test("søk navn henter fra Tp") {

        val navn = "Ola Nordmann"

        coEvery { tpService.getLeverandorNavn(any()) } returns TpResponse(navn)

        val result = skjermingService.getNavnForGjelderId("80000000001", navIdent)

        result shouldBe GjelderIdResponse(navn)
    }

    test("søk navn henter fra Ereg") {

        val navn = "Kari Nordmann"

        coEvery { eregService.getOrganisasjonsNavn(any()) } returns Organisasjon(Navn(navn))

        val result = skjermingService.getNavnForGjelderId("100000000", navIdent)

        result shouldBe GjelderIdResponse(navn)
    }

//    test("søk navn henter fra Pdl") {
//
//        coEvery { pdlService.getPersonNavn(any()) } returns Person(listOf(PdlNavn("Ola", "Heter", "Nordmann")))
//        coEvery { skjermetService.kanSaksbehandlerSePerson(any(), any()) } returns true
//
//        val result = integrationService.getNavnForGjelderId("10000000001", navIdent)
//
//        result shouldBe GjelderIdResponse("Ola Heter Nordmann")
//    }
//    test("søk navn henter fra Pdl født før 10. i en måned") {
//
//        coEvery { pdlService.getPersonNavn(any()) } returns Person(listOf(PdlNavn("Ola", "Heter", "Nordmann")))
//        coEvery { skjermetService.kanSaksbehandlerSePerson(any(), any()) } returns true
//
//        val result = integrationService.getNavnForGjelderId("01010212345", navIdent)
//
//        result shouldBe GjelderIdResponse("Ola Heter Nordmann")
//    }
})
