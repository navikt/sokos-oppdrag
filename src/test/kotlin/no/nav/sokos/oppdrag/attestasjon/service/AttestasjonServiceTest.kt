package no.nav.sokos.oppdrag.attestasjon.service

import kotlinx.serialization.json.Json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotliquery.queryOf

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.GJELDER_ID
import no.nav.sokos.oppdrag.attestasjon.Testdata.navIdent
import no.nav.sokos.oppdrag.attestasjon.Testdata.oppdragRequestTestdata
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.ALLE
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.EGEN_ATTESTERTE
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZosResponse
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOP
import no.nav.sokos.oppdrag.common.ENHETSNUMMER_NOS
import no.nav.sokos.oppdrag.common.valkey.ValkeyCache
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.listener.Db2Listener
import no.nav.sokos.oppdrag.listener.Db2Listener.attestasjonRepository
import no.nav.sokos.oppdrag.listener.Valkeylistener
import no.nav.sokos.oppdrag.security.AdGroup

internal class AttestasjonServiceTest :
    FunSpec({
        extensions(Valkeylistener, Db2Listener)

        val zosConnectService: ZOSConnectService = mockk<ZOSConnectService>()
        val skjermingService = mockk<SkjermingService>()
        val valkeyCache: ValkeyCache by lazy {
            ValkeyCache(name = "oppdrag", valkeyClient = Valkeylistener.valkeyClient)
        }

        val attestasjonService: AttestasjonService by lazy {
            AttestasjonService(
                attestasjonRepository = attestasjonRepository,
                zosConnectService = zosConnectService,
                skjermingService = skjermingService,
                valkeyCache = valkeyCache,
            )
        }

        afterEach {
            clearAllMocks()
            valkeyCache.getAllKeys().forEach { valkeyCache.delete(it) }
        }

        test("getOppdrag for en gjelderId når saksbehandler har skjermingtilgang til personen") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName, AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 8
            val oppdrag = result.data.first()
            oppdrag.oppdragsId shouldBe 25798519
            oppdrag.antAttestanter shouldBe 1
            oppdrag.navnFaggruppe shouldBe "HELSETJENESTER FRIKORT TAK 1 OG 2"
            oppdrag.navnFagomraade shouldBe "Egenandelsrefusjon frikort tak 1"
            oppdrag.fagSystemId shouldBe "13175913"
            oppdrag.oppdragGjelderId shouldBe "24029428499"
            oppdrag.kodeFaggruppe shouldBe "FRIKORT"
            oppdrag.kodeFagomraade shouldBe "FRIKORT1"
            oppdrag.kostnadssted shouldBe "2360"
            oppdrag.ansvarssted shouldBe "2340"
            oppdrag.erSkjermetForSaksbehandler shouldBe false
            oppdrag.hasWriteAccess shouldBe true

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("getOppdrag for faggruppe som ikke attestert når saksbehandler har skjermingtilgang") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName, AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName))
            coEvery { skjermingService.getSkjermingForIdentListe(listOf(GJELDER_ID), any()) } returns mapOf(GJELDER_ID to false)

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata.copy(gjelderId = null, kodeFagOmraade = "FRIKORT1"), navIdent)
            result.data.size shouldBe 3
            result.data.first { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.kodeFagomraade shouldBe "FRIKORT1"
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdent(any(), any()) }
        }

        test("getOppdrag som innholder kun egen attestert oppdragslinjer") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val ident = "K2786FPW"
            val navIdent =
                navIdent.copy(
                    ident = ident,
                    roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName, AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName),
                )
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata.copy(attestertStatus = EGEN_ATTESTERTE), navIdent)
            result.data.size shouldBe 2

            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true

                attestasjonRepository.getAttestanterWithOppdragsId(oppdrag.oppdragsId).values.flatten() shouldContain ident
            }
        }

        test("hent oppdrag for en gjelderId når saksbehandler har bare lesetilgang til NOS") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NOS_READ.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 3
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
                oppdrag.ansvarssted shouldBe null
                oppdrag.kostnadssted shouldBe ENHETSNUMMER_NOS
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent oppdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOS") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NOS_READ.adGroupName, AdGroup.ATTESTASJON_NOS_WRITE.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 3
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
                oppdrag.ansvarssted shouldBe null
                oppdrag.kostnadssted shouldBe ENHETSNUMMER_NOS
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOP") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NOP_READ.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 2
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
                (oppdrag.ansvarssted == ENHETSNUMMER_NOP || oppdrag.kostnadssted == ENHETSNUMMER_NOP) shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOP") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NOP_READ.adGroupName, AdGroup.ATTESTASJON_NOP_WRITE.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 2
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
                (oppdrag.ansvarssted == ENHETSNUMMER_NOP || oppdrag.kostnadssted == ENHETSNUMMER_NOP) shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til Nasjonalt") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 8
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent oppdrag for en gjelderId når saksbehandler har bare lesetilgang og skrivetilgang til Nasjonalt") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName, AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.data.size shouldBe 8
            result.data.forEach { oppdrag ->
                oppdrag.oppdragGjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("getOppdrag for en gjelderId kaster exception når saksbehandler ikke har tilgang til personen pga skjerming") {
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, navIdent) } returns true

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)

            result.errorMessage shouldBe "Mangler rettigheter til å se informasjon!"
        }

        test("hent oppdrag kaster exception hvis det er over 1000 forskjellige identer i identifiserte oppdrag") {
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NOS_READ.adGroupName))

            coEvery { attestasjonRepository.getOppdrag(any(), any(), any(), any(), any()) } returns
                List(1001) {
                    Oppdrag(
                        antAttestanter = 1,
                        navnFaggruppe = "HELSETJENESTER FRIKORT TAK 1 OG 2",
                        navnFagomraade = "Egenandelsrefusjon frikort tak 1",
                        fagSystemId = it.toString(),
                        oppdragGjelderId = GJELDER_ID.dropLast(4) + String.format("%04d", it),
                        kodeFaggruppe = "FRIKORT",
                        kodeFagomraade = "FRIKORT1",
                        kostnadssted = ENHETSNUMMER_NOS,
                        ansvarssted = null,
                        oppdragsId = it,
                    )
                }

            coEvery { skjermingService.getSkjermingForIdentListe(any(), any()) } throws
                AssertionError("getSkjermingForIdentListe should not be called for more than 1000 idents")

            val response =
                attestasjonService.getOppdrag(
                    OppdragsRequest(
                        gjelderId = null,
                        fagSystemId = "2960",
                        kodeFagGruppe = null,
                        kodeFagOmraade = "MSRBAL",
                        attestertStatus = ALLE,
                    ),
                    navIdent,
                )

            response.errorMessage shouldBe "Oppgitte søkekriterier gir for stort treff. Vennligst avgrens søket."
        }

        test("getOppdragsdetaljer returnerer tom liste for et gitt oppdrag som ikke har attestasjonslinjer") {
            val result = attestasjonService.getOppdragsdetaljer(92345678, navIdent)
            result shouldBe OppdragsdetaljerDTO(emptyList(), navIdent.ident)
        }

        test("getOppdragsDetaljer returnerer riktig datasett for et gitt scenario med UFOREUT") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdragsdetaljer_uforeut.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val oppdragsId = 58308587
            val result = attestasjonService.getOppdragsdetaljer(oppdragsId, navIdent)
            result.saksbehandlerIdent shouldBe navIdent.ident
            result.oppdragsLinjeList.size shouldBe 6

            val oppdragslinjeDTOList: List<OppdragslinjeDTO> = Json.decodeFromString("testdata/oppdragslinjeDTO_UFOREUT.json".readFromResource())
            result.oppdragsLinjeList shouldContainExactly oppdragslinjeDTOList
        }

        test("getOppdragsDetaljer returnerer riktig datasett for et gitt scenario med tre parallelle ytelser") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdragsdetaljer_parallelle_ytelser.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val oppdragsId = 1911991
            val result = attestasjonService.getOppdragsdetaljer(oppdragsId, navIdent)
            result.saksbehandlerIdent shouldBe navIdent.ident
            result.oppdragsLinjeList.size shouldBe 10

            val oppdragslinjeDTOList: List<OppdragslinjeDTO> = Json.decodeFromString("testdata/oppdragslinjeDTO_parallell_ytelser.json".readFromResource())
            result.oppdragsLinjeList shouldContainExactly oppdragslinjeDTOList

            // Det er 2 attestasjoner på linjen med id 1
            val enAvLinjene = result.oppdragsLinjeList.filter { l -> l.oppdragsLinje.linjeId == 1 }

            // Sjekker at begge kommer med i svaret
            enAvLinjene.size shouldBe 1
            enAvLinjene.first().attestasjonList.size shouldBe 2
        }

        test("attestereOppdrag vellykket og 1 linje er oppdatert") {
            val request =
                AttestasjonRequest(
                    "12345678900",
                    "98765432100",
                    "BEH",
                    999999999,
                    listOf(
                        AttestasjonLinje(
                            99999,
                            "Z999999",
                            "2021-01-01",
                        ),
                    ),
                )

            val response =
                ZosResponse(
                    "Oppdatering vellykket. 1 linjer oppdatert",
                )

            coEvery { zosConnectService.attestereOppdrag(any(), any()) } returns response
            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName))

            attestasjonService.attestereOppdrag(request, navIdent) shouldBe response
        }

        test("attestereOppdrag kaster exception når saksbehandler ikke har tilgang til personen pga. skjerming") {
            val request =
                AttestasjonRequest(
                    "12345678900",
                    "98765432100",
                    "BEH",
                    999999999,
                    listOf(
                        AttestasjonLinje(
                            99999,
                            "Z999999",
                            "2021-01-01",
                        ),
                    ),
                )

            val navIdent = navIdent.copy(roller = listOf(AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName))
            coEvery { skjermingService.getSkjermingForIdent(request.gjelderId, navIdent) } returns true

            val result = attestasjonService.attestereOppdrag(request, navIdent)

            result.errorMessage shouldBe "Mangler rettigheter til å attestere oppdrag!"
        }
    })
