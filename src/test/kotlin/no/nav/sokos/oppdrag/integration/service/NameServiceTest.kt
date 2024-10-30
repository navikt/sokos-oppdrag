package no.nav.sokos.oppdrag.integration.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.integration.client.ereg.EregClientService
import no.nav.sokos.oppdrag.integration.client.ereg.Organisasjon
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.tp.Leverandor
import no.nav.sokos.oppdrag.integration.client.tp.TpClientService
import no.nav.pdl.hentpersonbolk.Navn as PdlNavn
import no.nav.sokos.oppdrag.integration.client.ereg.Navn as EregNavn

private val pdlClientService = mockk<PdlClientService>()
private val tpClientService = mockk<TpClientService>()
private val eregClientService = mockk<EregClientService>()
private val nameService = NameService(pdlClientService, tpClientService, eregClientService)

internal class NameServiceTest : FunSpec({

    test("hent navn for gjelderId som er fnr uten mellomnavn") {

        val fnr = "12345678912"

        coEvery { pdlClientService.getPerson(any()) } returns
            mapOf(
                fnr to
                    Person(
                        listOf(PdlNavn("Ola", null, "Nordmann")),
                        listOf(),
                    ),
            )

        val name = nameService.getNavn(fnr, navIdent)

        name shouldBe NameResponse("Ola Nordmann")
    }

    test("hent navn for gjelderId som er fnr med mellomnavn") {

        val fnr = "12345678912"

        coEvery { pdlClientService.getPerson(any()) } returns
            mapOf(
                fnr to
                    Person(
                        listOf(PdlNavn("Kari", "Hermegåsa", "Nordmann")),
                        listOf(),
                    ),
            )

        val name = nameService.getNavn(fnr, navIdent)

        name shouldBe NameResponse("Kari Hermegåsa Nordmann")
    }

    test("hent navn for gjelderId som er orgnr") {

        val orgnr = "123456789"

        coEvery { eregClientService.getOrganisasjonsNavn(any()) } returns
            Organisasjon(EregNavn("NAV Arbeid og ytelser"))

        val name = nameService.getNavn(orgnr, navIdent)

        name shouldBe NameResponse("NAV Arbeid og ytelser")
    }

    test("hent navn for gjelderId som er leverandørId") {

        val leverandorId = "1234567890123"

        coEvery { tpClientService.getLeverandorNavn(any()) } returns
            Leverandor("NAV Arbeid og ytelser")

        val name = nameService.getNavn(leverandorId, navIdent)

        name shouldBe NameResponse("NAV Arbeid og ytelser")
    }
})
