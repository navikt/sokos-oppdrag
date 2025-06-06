package no.nav.sokos.oppdrag.integration.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.pdl.hentpersonbolk.Adressebeskyttelse
import no.nav.pdl.hentpersonbolk.Navn
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.valkey.ValkeyCache
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.skjerming.SkjermetClientService
import no.nav.sokos.oppdrag.listener.Valkeylistener
import no.nav.sokos.oppdrag.security.AdGroup

private val pdlClientService = mockk<PdlClientService>()
private val skjermetClientService = mockk<SkjermetClientService>()

internal class SkjermingServiceTest :
    FunSpec({
        extensions(Valkeylistener)

        val valkeyCache: ValkeyCache by lazy {
            ValkeyCache(
                name = "skjermingService",
                valkeyClient = Valkeylistener.valkeyClient,
            )
        }

        val skjermingService: SkjermingService by lazy {
            SkjermingService(
                pdlClientService = pdlClientService,
                skjermetClientService = skjermetClientService,
                valkeyCache = valkeyCache,
            )
        }

        test("saksbehandler med tilgang til egne ansatte skal kunne se person som er skjermet") {

            val ident = "12045678912"
            val navIdentMedEgneAnsatte = NavIdent("Z999999", listOf(AdGroup.EGNE_ANSATTE.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            emptyList(),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to true,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedEgneAnsatte)

            erPersonSkjermetForSakbehandler shouldBe false
        }

        test("saksbehandler med strengt fortrolig rolle skal kunne se person med adressebeskyttelse strengt fortrolig") {

            val ident = "12045678913"
            val navIdentMedStrengtFortrolig = NavIdent("Z999999", listOf(AdGroup.STRENGT_FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedStrengtFortrolig)

            erPersonSkjermetForSakbehandler shouldBe false
        }

        test("saksbehandler med fortrolig skal kunne se person med adressebeskyttelse fortrolig") {

            val ident = "12045678914"
            val navIdentMedFortrolig = NavIdent("Z999999", listOf(AdGroup.FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedFortrolig)

            erPersonSkjermetForSakbehandler shouldBe false
        }

        test("saksbehandler med tilgang til fortrolig skal ikke få se person med adressebeskyttelse strengt fortrolig") {

            val ident = "12045678915"
            val navIdentUtenTilgang = NavIdent("Z999999", listOf(AdGroup.FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentUtenTilgang)

            erPersonSkjermetForSakbehandler shouldBe true
        }

        test("saksbehandler med tilgang til egne ansatte skal ikke få se person med adressebeskyttelse fortrolig") {

            val ident = "12045678916"
            val navIdentUtenTilgang = NavIdent("Z999999", listOf(AdGroup.EGNE_ANSATTE.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentUtenTilgang)

            erPersonSkjermetForSakbehandler shouldBe true
        }

        test("sjekk at saksbehandler uten tilgang til egne ansatte ikke får tilgang til enkeltperson som er skjermet med egen ansatte") {

            val ident = "12045678917"
            val navIdentUtenTilgang = NavIdent("Z999999", listOf(AdGroup.FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            emptyList(),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to true,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentUtenTilgang)

            erPersonSkjermetForSakbehandler shouldBe true
        }

        test("sjekk at saksbehandler med tilgang til strengt fortrolig kan se personer med strengt fortrolig utland") {

            val ident = "12045678924"
            val navIdentMedStrengtFortrolig = NavIdent("Z999999", listOf(AdGroup.STRENGT_FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedStrengtFortrolig)

            erPersonSkjermetForSakbehandler shouldBe false
        }

        test("sjekk at saksbehandler med fortrolig ikke får tilgang til strengt fortrolig utland") {

            val ident = "12045678927"
            val navIdentMedFortrolig = NavIdent("Z999999", listOf(AdGroup.FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                )

            val erPersonSkjermetForSakbehandler = skjermingService.getSkjermingForIdent(ident, navIdentMedFortrolig)

            erPersonSkjermetForSakbehandler shouldBe true
        }

        test("saksbehandler har tilgang til egne ansatte og skal kun se en av personene siden den ene er skjermet") {

            val ident1 = "12045678918"
            val ident2 = "12045678919"
            val navIdentMedEgneAnsatte = NavIdent("Z999999", listOf(AdGroup.EGNE_ANSATTE.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident1 to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            emptyList(),
                        ),
                    ident2 to
                        Person(
                            listOf(Navn("Kari", null, "Nordmann")),
                            emptyList(),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident1 to false,
                    ident2 to true,
                )

            val erPersonSkjermetForSakbehandlerMap = skjermingService.getSkjermingForIdentListe(listOf(ident1, ident2), navIdentMedEgneAnsatte)

            erPersonSkjermetForSakbehandlerMap[ident1] shouldBe false
            erPersonSkjermetForSakbehandlerMap[ident2] shouldBe false
        }

        test("saksbehandler skal kunne se personer når hen har tilgang til strengt fortrolig og personene har adressebeskyttelse strengt fortrolig") {

            val ident1 = "12045678920"
            val ident2 = "12045678921"
            val navIdentMedStrengtFortrolig = NavIdent("Z999999", listOf(AdGroup.STRENGT_FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident1 to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                            ),
                        ),
                    ident2 to
                        Person(
                            listOf(Navn("Kari", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident1 to false,
                    ident2 to false,
                )

            val erPersonSkjermetForSakbehandlerMap = skjermingService.getSkjermingForIdentListe(listOf(ident1, ident2), navIdentMedStrengtFortrolig)

            erPersonSkjermetForSakbehandlerMap[ident1] shouldBe false
            erPersonSkjermetForSakbehandlerMap[ident2] shouldBe false
        }

        test("saksbehandler skal se personer når hen har tilgang til fortrolig og personene har adressebeskyttelse fortrolig") {

            val ident1 = "12045678922"
            val ident2 = "12045678923"
            val navIdentMedFortrolig = NavIdent("Z999999", listOf(AdGroup.FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident1 to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG),
                            ),
                        ),
                    ident2 to
                        Person(
                            listOf(Navn("Kari", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident1 to false,
                    ident2 to false,
                )

            val erPersonSkjermetForSakbehandlerMap = skjermingService.getSkjermingForIdentListe(listOf(ident1, ident2), navIdentMedFortrolig)

            erPersonSkjermetForSakbehandlerMap[ident1] shouldBe false
            erPersonSkjermetForSakbehandlerMap[ident2] shouldBe false
        }

        test("har saksbehandler ingen tilganger og personene er ikke skjermet skal de få tilgang") {

            val ident = "12045678924"
            val ident2 = "12045678925"
            val navIdentUtenTilgangTilSkjerming = NavIdent("Z999999", emptyList())

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            emptyList(),
                        ),
                    ident2 to
                        Person(
                            listOf(Navn("Kari", null, "Nordmann")),
                            emptyList(),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident to false,
                    ident2 to false,
                )

            val erPersonSkjermetForSakbehandlerMap = skjermingService.getSkjermingForIdentListe(listOf(ident, ident2), navIdentUtenTilgangTilSkjerming)

            erPersonSkjermetForSakbehandlerMap[ident] shouldBe false
            erPersonSkjermetForSakbehandlerMap[ident2] shouldBe false
        }

        test("saksbehandler skal ha tilgang til å se person når hen har tilgang til strengt fortrolig og personen har adressebeskyttelse strengt fortrolig og strengt fortrolig utland") {

            val ident1 = "12045678926"
            val navIdentMedStrengtFortrolig = NavIdent("Z999999", listOf(AdGroup.STRENGT_FORTROLIG.adGroupName))

            coEvery { pdlClientService.getPerson(any()) } returns
                mapOf(
                    ident1 to
                        Person(
                            listOf(Navn("Ola", null, "Nordmann")),
                            listOf(
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                                Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND),
                            ),
                        ),
                )

            coEvery { skjermetClientService.isSkjermedePersonerInSkjermingslosningen(any()) } returns
                mapOf(
                    ident1 to false,
                )

            val erPersonSkjermetForSakbehandlerMap = skjermingService.getSkjermingForIdentListe(listOf(ident1), navIdentMedStrengtFortrolig)

            erPersonSkjermetForSakbehandlerMap[ident1] shouldBe false
        }
    })
