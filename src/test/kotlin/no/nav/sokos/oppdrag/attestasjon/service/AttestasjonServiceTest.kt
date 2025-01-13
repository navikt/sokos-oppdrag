package no.nav.sokos.oppdrag.attestasjon.service

import kotlinx.serialization.json.Json

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.EGEN_ATTESTERTE
import no.nav.sokos.oppdrag.attestasjon.api.model.ZosResponse
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragslinjeDTO
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOP_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOP_WRITE
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOS_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOS_WRITE
import no.nav.sokos.oppdrag.common.redis.RedisCache
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.listener.Db2Listener
import no.nav.sokos.oppdrag.listener.Db2Listener.attestasjonRepository
import no.nav.sokos.oppdrag.listener.Db2Listener.fagomraadeRepository
import no.nav.sokos.oppdrag.listener.RedisListener

internal class AttestasjonServiceTest :
    FunSpec({
        extensions(RedisListener, Db2Listener)

        val zosConnectService: ZOSConnectService = mockk<ZOSConnectService>()
        val skjermingService = mockk<SkjermingService>()
        val redisCache: RedisCache by lazy {
            RedisCache(name = "oppdrag", redisClient = RedisListener.redisClient)
        }

        val attestasjonService: AttestasjonService by lazy {
            AttestasjonService(
                attestasjonRepository = attestasjonRepository,
                fagomraadeRepository = fagomraadeRepository,
                zosConnectService = zosConnectService,
                skjermingService = skjermingService,
                redisCache = redisCache,
            )
        }

        afterEach {
            clearAllMocks()
            redisCache.getAllKeys().forEach { redisCache.delete(it) }
        }

        test("getFagOmraader skal returnere en liste med FagOmraade") {
            val fagomraader = attestasjonService.getFagOmraader()
            fagomraader.size shouldBe 293
            fagomraader.forEach {
                it.kode shouldNotBe null
                it.kode shouldNotBe null
            }
        }

        test("getOppdrag for en gjelderId når saksbehandler har skjermingtilgang til personen") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 16
            val oppdrag = result.first()
            oppdrag.oppdragsId shouldBe 58308587
            oppdrag.antallAttestanter shouldBe 1
            oppdrag.fagGruppe shouldBe "Inntektsytelser"
            oppdrag.fagOmraade shouldBe "Uføretrygd"
            oppdrag.fagSystemId shouldBe "25444802"
            oppdrag.gjelderId shouldBe "24029428499"
            oppdrag.kodeFagGruppe shouldBe "INNT"
            oppdrag.kodeFagOmraade shouldBe "UFOREUT"
            oppdrag.kostnadsSted shouldBe "4402"
            oppdrag.ansvarsSted shouldBe "4819"
            oppdrag.erSkjermetForSaksbehandler shouldBe false
            oppdrag.hasWriteAccess shouldBe true

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("getOppdrag for faggruppe som ikke attestert når saksbehandler har skjermingtilgang") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            coEvery { skjermingService.getSkjermingForIdentListe(listOf(GJELDER_ID), any()) } returns mapOf(GJELDER_ID to false)

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata.copy(gjelderId = null, kodeFagOmraade = "FRIKORT1"), navIdent)
            result.size shouldBe 9
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.kodeFagOmraade shouldBe "FRIKORT1"
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
                    roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE),
                )
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata.copy(attestertStatus = EGEN_ATTESTERTE), navIdent)
            result.size shouldBe 2

            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true

                attestasjonRepository.getAttestanterWithOppdragsId(oppdrag.oppdragsId).values.flatten() shouldContain ident
            }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOS") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 4
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
                oppdrag.ansvarsSted shouldBe null
                oppdrag.kostnadsSted shouldBe ENHETSNUMMER_NOS
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOS") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ, GRUPPE_ATTESTASJON_NOS_WRITE))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 4
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
                oppdrag.ansvarsSted shouldBe null
                oppdrag.kostnadsSted shouldBe ENHETSNUMMER_NOS
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOP") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOP_READ))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 3
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
                (oppdrag.ansvarsSted == ENHETSNUMMER_NOP || oppdrag.kostnadsSted == ENHETSNUMMER_NOP) shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOP") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOP_READ, GRUPPE_ATTESTASJON_NOP_WRITE))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 3
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
                (oppdrag.ansvarsSted == ENHETSNUMMER_NOP || oppdrag.kostnadsSted == ENHETSNUMMER_NOP) shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til Landsdekkende") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 16
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe false
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang og skrivetilgang til Landsdekkende") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
            result.size shouldBe 16
            result.forEach { oppdrag ->
                oppdrag.gjelderId shouldBe GJELDER_ID
                oppdrag.erSkjermetForSaksbehandler shouldBe false
                oppdrag.hasWriteAccess shouldBe true
            }

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("getOppdrag for en gjelderId kaster exception når saksbehandler ikke har tilgang til personen pga. skjerming") {
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, navIdent) } returns true

            val exception =
                shouldThrow<AttestasjonException> {
                    attestasjonService.getOppdrag(oppdragRequestTestdata, navIdent)
                }

            exception.message shouldBe "Mangler rettigheter til å se informasjon!"
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
            result.linjer.size shouldBe 9

            val oppdragslinjeDTOList: List<OppdragslinjeDTO> = Json.decodeFromString("testdata/OppdragslinjeDTO_UFOREUT.json".readFromResource())
            result.linjer shouldContainExactly oppdragslinjeDTOList
        }

        test("getOppdragsDetaljer returnerer riktig datasett for et gitt scenario med tre parallelle ytelser") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/attestasjon/getOppdragsdetaljer_parallelle_ytelser.sql".readFromResource())) shouldBeGreaterThan 0
            }
            val oppdragsId = 1911991
            val result = attestasjonService.getOppdragsdetaljer(oppdragsId, navIdent)
            result.saksbehandlerIdent shouldBe navIdent.ident
            result.linjer.size shouldBe 24

            val oppdragslinjeDTOList: List<OppdragslinjeDTO> = Json.decodeFromString("testdata/OppdragslinjeDTO_parallell_ytelser.json".readFromResource())
            result.linjer shouldContainExactly oppdragslinjeDTOList

            // Det er 2 attestasjoner på linjen med id 1
            val enAvLinjene = result.linjer.filter { l -> l.oppdragsLinje.linjeId == 1 }

            // Sjekker at begge kommer med i svaret
            enAvLinjene.size shouldBe 1
            enAvLinjene.first().attestasjoner.size shouldBe 2
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
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))

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

            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ))
            coEvery { skjermingService.getSkjermingForIdent(request.gjelderId, navIdent) } returns true

            val exception =
                shouldThrow<AttestasjonException> {
                    attestasjonService.attestereOppdrag(request, navIdent)
                }

            exception.message shouldBe "Mangler rettigheter til å attestere oppdrag!"
        }
    })
