package no.nav.sokos.oppdrag.integration.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.pdl.hentpersonbolk.Navn
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.common.GRUPPE_EGNE_ANSATTE
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.skjerming.SkjermetClientService

private val pdlClientService = mockk<PdlClientService>()
private val skjermingClientService = mockk<SkjermetClientService>()
val skjermingService = SkjermingService(pdlClientService, skjermingClientService)

private val navIdentMedRoller = NavIdent("Z999999", listOf(GRUPPE_EGNE_ANSATTE))

internal class SkjermingServiceTest : FunSpec({

    test("sjekk skjerming for enkeltperson når saksbehandler har tilgang til egne ansatte og personen er skjermet") {

        val ident = "12345678912"

        coEvery { pdlClientService.getPerson(any()) } returns
            mapOf(
                ident to
                    Person(
                        listOf(Navn("Ola", null, "Nordmann")),
                        emptyList(),
                    ),
            )

        coEvery { skjermingClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
            mapOf(
                ident to true,
            )

        val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedRoller)

        erPersonSkjermetForSakbehandler shouldBe false
    }
})
