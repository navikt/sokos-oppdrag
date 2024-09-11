package no.nav.sokos.oppdrag.attestasjon.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragslinjeWithoutFluff
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponse
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import java.time.LocalDate

private val applicationCall = mockk<ApplicationCall>()
private val attestasjonRepository = mockk<AttestasjonRepository>()
private val zosConnectService: ZOSConnectService = mockk<ZOSConnectService>()
private val attestasjonService = AttestasjonService(attestasjonRepository, zosConnectService = zosConnectService)

internal class AttestasjonServiceTest : FunSpec({

    beforeTest {
        every { applicationCall.request.headers["Authorization"] } returns tokenWithNavIdent
        every { attestasjonRepository.getOppdrag(any(), any(), any(), any(), any()) } returns emptyList()
    }

    test("attestasjon av oppdrag") {
        val oppdragsid = 999999999

        val request =
            AttestasjonRequest(
                gjelderId = "12345678900",
                fagSystemId = "98765432100",
                kodeFagOmraade = "BEH",
                oppdragsId = oppdragsid,
                linjer =
                    listOf(
                        AttestasjonLinje(
                            linjeId = 99999,
                            attestantIdent = "Z999999",
                            datoUgyldigFom = "string",
                        ),
                    ),
            )

        val response =
            PostOSAttestasjonResponse200(
                osAttestasjonOperationResponse =
                    PostOSAttestasjonResponse200OSAttestasjonOperationResponse(
                        attestasjonskvittering =
                            PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvittering(
                                responsAttestasjon =
                                    PostOSAttestasjonResponse200OSAttestasjonOperationResponseAttestasjonskvitteringResponsAttestasjon(
                                        gjelderId = "string",
                                        oppdragsId = 999999999,
                                        antLinjerMottatt = 99999,
                                        statuskode = 99,
                                        melding = "string",
                                    ),
                            ),
                    ),
            )

        coEvery { zosConnectService.attestereOppdrag(any(), any()) } returns response
        attestasjonService.attestereOppdrag(applicationCall, request) shouldBe response
    }

    test("tilrettelegging av oppdragslinjer for attestering 1") {
        every { attestasjonRepository.getOppdragslinjerWithoutFluff(any()) } returns
            withoutFluff(
                """
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE                                       |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID                  |
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                |58308587   |6       |UFOREUT                                           |2021-12-01     |2021-12-31     |N        |23867.00|MND      |759868829                     |
                |58308587   |7       |UFOREUT                                           |2022-01-01     |2022-01-31     |J        |23867.00|MND      |759868830                     |
                |58308587   |8       |UFOREUT                                           |2022-02-01     |2022-04-30     |J        |21816.00|MND      |759868831                     |
                |58308587   |9       |UFOREUT                                           |2022-05-01     |null           |J        |22857.00|MND      |759868832                     |
                |58308587   |10      |UFOREUT                                           |2023-01-01     |null           |J        |22857.00|MND      |771253891                     |
                |58308587   |11      |UFOREUT                                           |2023-05-01     |null           |N        |24322.00|MND      |801686775                     |
                |58308587   |12      |UFOREUT                                           |2024-01-01     |null           |J        |24322.00|MND      |839410565                     |
                |58308587   |13      |UFOREUT                                           |2024-05-01     |null           |N        |25431.00|MND      |861989539                     |
                |58308587   |14      |UFOREUT                                           |2024-07-01     |null           |N        |25899.00|MND      |878336072                     |
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                """.trimIndent(),
            )

        attestasjonService.getOppdragsDetaljer(12345678) shouldBe emptyList()
    }

    test("tilrettelegging av oppdragslinjer for attestering") {
        every { attestasjonRepository.getOppdragslinjerWithoutFluff(any()) } returns
            withoutFluff(
                """
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                |OPPDRAGS_ID|LINJE_ID|KODE_KLASSE                                       |DATO_VEDTAK_FOM|DATO_VEDTAK_TOM|ATTESTERT|SATS    |TYPE_SATS|DELYTELSE_ID                  |
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                |63308761   |2       |PENAPGP                                           |2022-12-01     |null           |J        |2005.00 |MND      |756727211                     |
                |63308761   |8       |PENAPGP                                           |2023-04-01     |null           |J        |2025.00 |MND      |786995290                     |
                |63308761   |10      |PENAPGP                                           |2023-05-01     |null           |J        |2162.00 |MND      |795307286                     |
                |63308761   |14      |PENAPGP                                           |2024-02-01     |null           |J        |2185.00 |MND      |839618861                     |
                |63308761   |16      |PENAPGP                                           |2024-05-01     |null           |J        |2255.00 |MND      |863138304                     |
                |63308761   |1       |PENAPIP                                           |2022-12-01     |null           |N        |16524.00|MND      |756727209                     |
                |63308761   |7       |PENAPIP                                           |2023-04-01     |null           |J        |17125.00|MND      |786995288                     |
                |63308761   |11      |PENAPIP                                           |2023-05-01     |null           |J        |18284.00|MND      |795307289                     |
                |63308761   |13      |PENAPIP                                           |2024-02-01     |null           |J        |18945.00|MND      |839618859                     |
                |63308761   |18      |PENAPIP                                           |2024-05-01     |null           |J        |19553.00|MND      |863138307                     |
                |63308761   |3       |PENAPTP                                           |2022-12-01     |null           |J        |5731.00 |MND      |756727212                     |
                |63308761   |9       |PENAPTP                                           |2023-04-01     |null           |J        |5799.00 |MND      |786995291                     |
                |63308761   |12      |PENAPTP                                           |2023-05-01     |null           |J        |6191.00 |MND      |795307288                     |
                |63308761   |15      |PENAPTP                                           |2024-02-01     |null           |J        |6256.00 |MND      |839618862                     |
                |63308761   |17      |PENAPTP                                           |2024-05-01     |null           |J        |6457.00 |MND      |863138305                     |
                +-----------+--------+--------------------------------------------------+---------------+---------------+---------+--------+---------+------------------------------+
                """.trimIndent(),
            )

        attestasjonService.getOppdragsDetaljer(12345678) shouldBe emptyList()
    }
})

private fun withoutFluff(pretty: String): List<OppdragslinjeWithoutFluff> {
    return pretty.split("\n").filter { s -> s.isNotBlank() && !s.contains("-----") && !s.contains("OPPDRAGS_ID") }
        .map { s -> mapToOppdragslinjeWithoutFluff(s.split("|").map { s -> s.trim() }.toList()) }
}

private fun mapToOppdragslinjeWithoutFluff(params: List<String>): OppdragslinjeWithoutFluff {
    return OppdragslinjeWithoutFluff(
        oppdragsId = params.get(1).toInt(),
        linjeId = params.get(2).toInt(),
        kodeKlasse = params.get(3),
        datoVedtakFom = LocalDate.parse(params.get(4)),
        datoVedtakTom = if (params.get(5).equals("null")) null else LocalDate.parse(params.get(5)),
        attestert = (if (params.get(6).equals("J")) true else false),
        sats = params.get(7).toDouble(),
        typeSats = params.get(8),
        delytelseId = params.get(9).toInt(),
    )
}
