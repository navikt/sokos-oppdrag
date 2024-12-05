package no.nav.sokos.oppdrag.attestasjon.service

import com.github.benmanes.caffeine.cache.Caffeine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import no.nav.sokos.oppdrag.TestUtil.navIdent
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZOsResponse
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO
import no.nav.sokos.oppdrag.attestasjon.dto.OppdragsdetaljerDTO
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.attestasjon.utils.GJELDER_ID
import no.nav.sokos.oppdrag.attestasjon.utils.KODE_FAGGRUPPE
import no.nav.sokos.oppdrag.attestasjon.utils.KODE_FAGOMRAADE
import no.nav.sokos.oppdrag.attestasjon.utils.Testdata.oppdragMockdata
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOP_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOP_WRITE
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOS_READ
import no.nav.sokos.oppdrag.common.GRUPPE_ATTESTASJON_NOS_WRITE
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import java.time.Duration

private val attestasjonRepository = mockk<AttestasjonRepository>()
private val zosConnectService: ZOSConnectService = mockk<ZOSConnectService>()
private val skjermingService = mockk<SkjermingService>()
private val oppdragCache =
    Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofMinutes(60))
        .maximumSize(10_000)
        .buildAsync<String, List<Oppdrag>>()
val attestasjonService =
    AttestasjonService(
        attestasjonRepository = attestasjonRepository,
        zosConnectService = zosConnectService,
        skjermingService = skjermingService,
        oppdragCache = oppdragCache,
    )

internal class AttestasjonServiceTest :
    FunSpec({

        afterEach {
            clearAllMocks()
            oppdragCache.synchronous().invalidateAll()
        }

        test("hent oppdrag for en gjelderId når saksbehandler har skjermingtilgang til personen") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            val oppdragList = listOf(oppdragMockdata)

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = true)
        }

        test("hent oppdrag for faggruppe som ikke attestert når saksbehandler har skjermingtilgang") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            val oppdragList = listOf(oppdragMockdata)

            coEvery { attestasjonRepository.getFagomraaderForFaggruppe(KODE_FAGGRUPPE) } returns listOf(KODE_FAGOMRAADE)
            coEvery { attestasjonRepository.getOppdrag(any(), any(), any(), listOf(KODE_FAGOMRAADE)) } returns oppdragList
            coEvery { skjermingService.getSkjermingForIdentListe(listOf(GJELDER_ID), any()) } returns mapOf(GJELDER_ID to false)

            val result = attestasjonService.getOppdrag(null, null, null, KODE_FAGOMRAADE, null, navIdent)
            result shouldBe oppdragList.map { it.toDTO(hasWriteAccess = true) }
            result.size shouldBe oppdragList.size

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdent(any(), any()) }
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOS") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ))
            val oppdragList = listOf(oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOS))

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = false)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOS") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ, GRUPPE_ATTESTASJON_NOS_WRITE))
            val oppdragList = listOf(oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOS))

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = true)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOP") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOP_READ))
            val oppdragList = listOf(oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOP))

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = false)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOP") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOP_READ, GRUPPE_ATTESTASJON_NOP_WRITE))
            val oppdragList = listOf(oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOP))

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = true)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til Landsdekkende") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ))
            val oppdragList = listOf(oppdragMockdata)

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = false)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang og skrivetilgang til Landsdekkende") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ, GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE))
            val oppdragList = listOf(oppdragMockdata)

            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = true)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang til NOS og NOP") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ, GRUPPE_ATTESTASJON_NOP_READ))
            val oppdragList =
                listOf(
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOS),
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOP),
                )
            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = false)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har både lesetilgang og skrivetilgang til NOS og NOP") {
            val navIdent =
                navIdent.copy(
                    roller =
                        listOf(
                            GRUPPE_ATTESTASJON_NOS_READ,
                            GRUPPE_ATTESTASJON_NOS_WRITE,
                            GRUPPE_ATTESTASJON_NOP_READ,
                            GRUPPE_ATTESTASJON_NOP_WRITE,
                        ),
                )
            val oppdragList =
                listOf(
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOS),
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOP),
                )
            verifyHentOppdrag(oppdragList, navIdent, hasWriteAccess = true)
        }

        test("hent opopdrag for en gjelderId når saksbehandler har bare lesetilgang til NOS og både lesetilgang og skrivetilgang til NOP") {
            val navIdent = navIdent.copy(roller = listOf(GRUPPE_ATTESTASJON_NOS_READ, GRUPPE_ATTESTASJON_NOP_READ, GRUPPE_ATTESTASJON_NOP_WRITE))
            val oppdragList =
                listOf(
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOS),
                    oppdragMockdata.copy(ansvarsSted = ENHETSNUMMER_NOP),
                )

            coEvery { attestasjonRepository.getOppdrag(any(), any(), GJELDER_ID, any()) } returns oppdragList
            coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

            val result = attestasjonService.getOppdrag(GJELDER_ID, null, null, null, null, navIdent)
            result[0] shouldBe oppdragList[0].toDTO(hasWriteAccess = false)
            result[1] shouldBe oppdragList[1].toDTO(hasWriteAccess = true)
            result.size shouldBe oppdragList.size

            coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
        }

        test("hent oppdrag for en gjelderId kaster exception når saksbehandler ikke har tilgang til personen pga. skjerming") {
            val gjelderId = "12345678900"

            coEvery { skjermingService.getSkjermingForIdent(gjelderId, navIdent) } returns true

            val exception =
                shouldThrow<AttestasjonException> {
                    attestasjonService.getOppdrag(gjelderId, null, null, null, null, navIdent)
                }

            exception.message shouldBe "Mangler rettigheter til å se informasjon!"
        }

        test("getOppdragsdetaljer returnerer tom liste for et gitt oppdrag som ikke har attestasjonslinjer") {

            every { attestasjonRepository.getOppdragslinjer(any()) } returns emptyList()
            every { attestasjonRepository.getEnhetForLinjer(any(), any(), any()) } returns emptyMap()
            every { attestasjonRepository.getAttestasjonerForLinjer(any(), any()) } returns emptyMap()

            val result = attestasjonService.getOppdragsdetaljer(92345678, navIdent)

            result shouldBe OppdragsdetaljerDTO(emptyList(), navIdent.ident)
        }

        test("getOppdragsDetaljer returnerer riktig datasett for et gitt scenario med UFOREUT") {

            every { attestasjonRepository.getOppdragslinjer(any()) } returns
                oppdragslinjer(
                    """
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |12345678   |6       |UFOREUT     |2021-12-01     |2021-12-31     |N        |23867.00|MND      |759868829    |
                    |12345678   |7       |UFOREUT     |2022-01-01     |2022-01-31     |J        |23867.00|MND      |759868830    |
                    |12345678   |8       |UFOREUT     |2022-02-01     |2022-04-30     |J        |21816.00|MND      |759868831    |
                    |12345678   |9       |UFOREUT     |2022-05-01     |null           |J        |22857.00|MND      |759868832    |
                    |12345678   |10      |UFOREUT     |2023-01-01     |null           |J        |22857.00|MND      |771253891    |
                    |12345678   |11      |UFOREUT     |2023-05-01     |null           |N        |24322.00|MND      |801686775    |
                    |12345678   |12      |UFOREUT     |2024-01-01     |null           |J        |24322.00|MND      |839410565    |
                    |12345678   |13      |UFOREUT     |2024-05-01     |null           |N        |25431.00|MND      |861989539    |
                    |12345678   |14      |UFOREUT     |2024-07-01     |null           |N        |25899.00|MND      |878336072    |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    """.trimIndent(),
                )

            every { attestasjonRepository.getEnhetForLinjer(any(), any(), any()) } returns emptyMap()
            every { attestasjonRepository.getAttestasjonerForLinjer(any(), any()) } returns
                attestasjoner(
                    """
                    +--------+------------+----------------+
                    |LINJE_ID|ATTESTANT_ID|DATO_UGYLDIG_FOM|
                    +--------+------------+----------------+
                    |1       |N168245     |9999-12-31      |
                    |2       |L170628     |9999-12-31      |
                    |3       |PPEN003     |9999-12-31      |
                    |7       |PPEN004     |9999-12-31      |
                    |8       |PPEN004     |9999-12-31      |
                    |9       |PPEN004     |9999-12-31      |
                    |10      |BPEN068     |9999-12-31      |
                    |11      |PPEN004     |9999-12-31      |
                    |12      |PPEN004     |9999-12-31      |
                    |13      |PPEN004     |9999-12-31      |
                    |14      |PPEN004     |9999-12-31      |
                    |15      |PPEN004     |9999-12-31      |
                    |16      |BPEN068     |9999-12-31      |
                    |17      |BPEN068     |9999-12-31      |
                    |18      |BPEN068     |9999-12-31      |
                    +--------+------------+----------------+

                    """.trimIndent(),
                )

            attestasjonService.getOppdragsdetaljer(12345678, navIdent).linjer.map { l -> l.oppdragsLinje } shouldContainExactly
                oppdragslinjer(
                    """
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |12345678   |6       |UFOREUT     |2021-12-01     |2021-12-31     |N        |23867.00|MND      |759868829    |
                    |12345678   |7       |UFOREUT     |2022-01-01     |2022-01-31     |J        |23867.00|MND      |759868830    |
                    |12345678   |8       |UFOREUT     |2022-02-01     |2022-04-30     |J        |21816.00|MND      |759868831    |
                    |12345678   |9       |UFOREUT     |2022-05-01     |2022-12-31     |J        |22857.00|MND      |759868832    |
                    |12345678   |10      |UFOREUT     |2023-01-01     |2023-04-30     |J        |22857.00|MND      |771253891    |
                    |12345678   |11      |UFOREUT     |2023-05-01     |2023-12-31     |N        |24322.00|MND      |801686775    |
                    |12345678   |12      |UFOREUT     |2024-01-01     |2024-04-30     |J        |24322.00|MND      |839410565    |
                    |12345678   |13      |UFOREUT     |2024-05-01     |2024-06-30     |N        |25431.00|MND      |861989539    |
                    |12345678   |14      |UFOREUT     |2024-07-01     |null           |N        |25899.00|MND      |878336072    |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    """.trimIndent(),
                )
        }

        test("getOppdragsDetaljer returnerer riktig datasett for et gitt scenario med tre parallelle ytelser") {

            every { attestasjonRepository.getOppdragslinjer(any()) } returns
                oppdragslinjer(
                    """
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |12345678   |2       |PENAPGP     |2022-12-01     |null           |J        |2005.00 |MND      |756727211    |
                    |12345678   |8       |PENAPGP     |2023-04-01     |null           |J        |2025.00 |MND      |786995290    |
                    |12345678   |10      |PENAPGP     |2023-05-01     |null           |J        |2162.00 |MND      |795307286    |
                    |12345678   |14      |PENAPGP     |2024-02-01     |null           |J        |2185.00 |MND      |839618861    |
                    |12345678   |16      |PENAPGP     |2024-05-01     |null           |J        |2255.00 |MND      |863138304    |
                    |12345678   |1       |PENAPIP     |2022-12-01     |null           |N        |16524.00|MND      |756727209    |
                    |12345678   |7       |PENAPIP     |2023-04-01     |null           |J        |17125.00|MND      |786995288    |
                    |12345678   |11      |PENAPIP     |2023-05-01     |null           |J        |18284.00|MND      |795307289    |
                    |12345678   |13      |PENAPIP     |2024-02-01     |null           |J        |18945.00|MND      |839618859    |
                    |12345678   |18      |PENAPIP     |2024-05-01     |null           |J        |19553.00|MND      |863138307    |
                    |12345678   |3       |PENAPTP     |2022-12-01     |null           |J        |5731.00 |MND      |756727212    |
                    |12345678   |9       |PENAPTP     |2023-04-01     |null           |J        |5799.00 |MND      |786995291    |
                    |12345678   |12      |PENAPTP     |2023-05-01     |null           |J        |6191.00 |MND      |795307288    |
                    |12345678   |15      |PENAPTP     |2024-02-01     |2024-03-31     |J        |6256.00 |MND      |839618862    |
                    |12345678   |17      |PENAPTP     |2024-05-01     |null           |J        |6457.00 |MND      |863138305    |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    """.trimIndent(),
                )

            every { attestasjonRepository.getEnhetForLinjer(any(), any(), any()) } returns emptyMap()
            every { attestasjonRepository.getAttestasjonerForLinjer(any(), any()) } returns
                attestasjoner(
                    """
                    +--------+------------+----------------+
                    |LINJE_ID|ATTESTANT_ID|DATO_UGYLDIG_FOM|
                    +--------+------------+----------------+
                    |1       |N168245     |9999-12-31      |
                    |2       |L170628     |9999-12-31      |
                    |3       |PPEN003     |9999-12-31      |
                    |7       |PPEN004     |9999-12-31      |
                    |8       |PPEN004     |9999-12-31      |
                    |9       |PPEN004     |9999-12-31      |
                    |10      |BPEN068     |9999-12-31      |
                    |11      |PPEN004     |9999-12-31      |
                    |12      |PPEN004     |9999-12-31      |
                    |13      |PPEN004     |9999-12-31      |
                    |13      |A313373     |9999-12-31      |
                    |14      |PPEN004     |9999-12-31      |
                    |15      |PPEN004     |9999-12-31      |
                    |16      |BPEN068     |9999-12-31      |
                    |17      |BPEN068     |9999-12-31      |
                    |18      |BPEN068     |9999-12-31      |
                    +--------+------------+----------------+

                    """.trimIndent(),
                )

            val oppdragsDetaljer = attestasjonService.getOppdragsdetaljer(12345678, navIdent)

            oppdragsDetaljer.linjer.size shouldBe 15

            // Det er 2 attestasjoner på linjen med id 13
            val enAvLinjene = oppdragsDetaljer.linjer.filter { l -> l.oppdragsLinje.linjeId == 13 }

            // Sjekker at begge kommer med i svaret
            enAvLinjene.size shouldBe 1
            enAvLinjene[0].attestasjoner.size shouldBe 2

            // Linje 15 har en til-og-med-dato satt til en måned før neste periode starter
            val linje15 = oppdragsDetaljer.linjer.filter { l -> l.oppdragsLinje.linjeId == 15 }
            linje15.size shouldBe 1

            // Men hvis det skjer så fall skal vi bruke den til-og-med-datoen som er oppgitt
            linje15[0].oppdragsLinje.datoVedtakTom shouldBe LocalDate.parse("2024-03-31")

            // Sjekker til-og-med-datoene
            // Etter at til-og-med-datoene er satt inn skal linjene overordnet sorteres til en rekkefølge
            // Basert på linjeid. Fra-og-med-dato og delytelsesid vil også være strengt stigende ved linjeid
            oppdragsDetaljer.linjer.map { l -> l.oppdragsLinje } shouldContainExactly
                oppdragslinjer(
                    """
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    |12345678   |1       |PENAPIP     |2022-12-01     |2023-03-31     |N        |16524.00|MND      |756727209    |
                    |12345678   |2       |PENAPGP     |2022-12-01     |2023-03-31     |J        |2005.00 |MND      |756727211    |
                    |12345678   |3       |PENAPTP     |2022-12-01     |2023-03-31     |J        |5731.00 |MND      |756727212    |
                    |12345678   |7       |PENAPIP     |2023-04-01     |2023-04-30     |J        |17125.00|MND      |786995288    |
                    |12345678   |8       |PENAPGP     |2023-04-01     |2023-04-30     |J        |2025.00 |MND      |786995290    |
                    |12345678   |9       |PENAPTP     |2023-04-01     |2023-04-30     |J        |5799.00 |MND      |786995291    |
                    |12345678   |10      |PENAPGP     |2023-05-01     |2024-01-31     |J        |2162.00 |MND      |795307286    |
                    |12345678   |11      |PENAPIP     |2023-05-01     |2024-01-31     |J        |18284.00|MND      |795307289    |
                    |12345678   |12      |PENAPTP     |2023-05-01     |2024-01-31     |J        |6191.00 |MND      |795307288    |
                    |12345678   |13      |PENAPIP     |2024-02-01     |2024-04-30     |J        |18945.00|MND      |839618859    |
                    |12345678   |14      |PENAPGP     |2024-02-01     |2024-04-30     |J        |2185.00 |MND      |839618861    |
                    |12345678   |15      |PENAPTP     |2024-02-01     |2024-03-31     |J        |6256.00 |MND      |839618862    |
                    |12345678   |16      |PENAPGP     |2024-05-01     |null           |J        |2255.00 |MND      |863138304    |
                    |12345678   |17      |PENAPTP     |2024-05-01     |null           |J        |6457.00 |MND      |863138305    |
                    |12345678   |18      |PENAPIP     |2024-05-01     |null           |J        |19553.00|MND      |863138307    |
                    +-----------+--------+------------+---------------+---------------+---------+--------+---------+-------------+
                    """.trimIndent(),
                )
        }

        test("attestasjon av oppdrag") {

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
                ZOsResponse(
                    "Oppdatering vellykket. 1 linjer oppdatert",
                )

            coEvery { zosConnectService.attestereOppdrag(any(), any()) } returns response
            attestasjonService.attestereOppdrag(request, navIdent) shouldBe response
        }
    })

private suspend fun verifyHentOppdrag(
    oppdragList: List<Oppdrag>,
    navIdent: NavIdent,
    hasWriteAccess: Boolean,
) {
    coEvery { attestasjonRepository.getOppdrag(any(), any(), GJELDER_ID, any()) } returns oppdragList
    coEvery { skjermingService.getSkjermingForIdent(GJELDER_ID, any()) } returns false

    val result = attestasjonService.getOppdrag(GJELDER_ID, null, null, null, null, navIdent)
    result shouldBe oppdragList.map { it.toDTO(hasWriteAccess = hasWriteAccess) }
    result.size shouldBe oppdragList.size

    coVerify(exactly = 0) { skjermingService.getSkjermingForIdentListe(any(), any()) }
}

private fun oppdragslinjer(pretty: String): List<Oppdragslinje> =
    pretty
        .split("\n")
        .filter { s -> s.isNotBlank() && !s.contains("-----") && !s.contains("OPPDRAGS_ID") }
        .map { s -> s.split("|").map { it.trim() }.toList() }
        .map { l -> mapToOppdragslinje(l) }

private fun attestasjoner(pretty: String): Map<Int, List<Attestasjon>> {
    val map =
        pretty
            .split("\n")
            .filter { s -> s.isNotBlank() && !s.contains("-----") && !s.contains("LINJE_ID") }
            .map { s -> s.split("|").map { it.trim() }.toList() }
            .map { l -> l[1].toInt() to mapToAttestasjon(l) }
    return map
        .groupBy({ it.first }, { it.second })
}

private fun mapToAttestasjon(params: List<String>): Attestasjon =
    Attestasjon(
        attestant = params[2],
        datoUgyldigFom = LocalDate.parse(params[3]),
    )

private fun mapToOppdragslinje(params: List<String>): Oppdragslinje =
    Oppdragslinje(
        attestert = (params[6] == "J"),
        datoVedtakFom = LocalDate.parse(params[4]),
        datoVedtakTom = if (params[5] == "null") null else LocalDate.parse(params[5]),
        delytelseId = params[9],
        kodeKlasse = params[3],
        linjeId = params[2].toInt(),
        oppdragsId = params[1].toInt(),
        sats = params[7].toDouble(),
        typeSats = params[8],
        grad = 100,
        kid = "5678901234567890",
        kontonummer = "12345678901",
        refusjonsid = null,
        skyldner = "98765432100",
        utbetalesTil = "12345678900",
    )
