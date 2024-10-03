package no.nav.sokos.oppdrag.integration.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.pdl.hentperson.Person
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdResponse
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.ereg.Navn
import no.nav.sokos.oppdrag.integration.ereg.Organisasjon
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.tp.TpResponse
import no.nav.sokos.oppdrag.integration.tp.TpService
import no.nav.pdl.hentperson.Navn as PdlNavn

private val eregService = mockk<EregService>()
private val tpService = mockk<TpService>()
private val pdlService = mockk<PdlService>()

private val integrationService = IntegrationService(pdlService = pdlService, tpService = tpService, eregService = eregService)

internal class IntegrationServiceTest : FunSpec({

    test("søk navn henter fra Tp") {

        val navn = "Ola Nordmann"

        coEvery { tpService.getLeverandorNavn(any()) } returns TpResponse(navn)

        val result = integrationService.getNavnForGjelderId("80000000001", navIdent)

        result shouldBe GjelderIdResponse(navn)
    }

    test("søk navn henter fra Ereg") {

        val navn = "Kari Nordmann"

        coEvery { eregService.getOrganisasjonsNavn(any()) } returns Organisasjon(Navn(navn))

        val result = integrationService.getNavnForGjelderId("100000000", navIdent)

        result shouldBe GjelderIdResponse(navn)
    }

    test("søk navn henter fra Pdl") {

        coEvery { pdlService.getPersonNavn(any()) } returns Person(listOf(PdlNavn("Ola", "Heter", "Nordmann")))

        val result = integrationService.getNavnForGjelderId("10000000001", navIdent)

        result shouldBe GjelderIdResponse("Ola Heter Nordmann")
    }
    test("søk navn henter fra Pdl født før 10. i en måned") {

        coEvery { pdlService.getPersonNavn(any()) } returns Person(listOf(PdlNavn("Ola", "Heter", "Nordmann")))

        val result = integrationService.getNavnForGjelderId("01010212345", navIdent)

        result shouldBe GjelderIdResponse("Ola Heter Nordmann")
    }
})
