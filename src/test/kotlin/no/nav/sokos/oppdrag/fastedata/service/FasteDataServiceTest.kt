package no.nav.sokos.oppdrag.fastedata.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode
import no.nav.sokos.oppdrag.listener.Db2Listener

internal class FasteDataServiceTest : FunSpec({
    extensions(Db2Listener)

    val fastedataService =
        FasteDataService(
            Db2Listener.fasteDataFagomraadeRepository,
            Db2Listener.venteKriterierRepository,
            Db2Listener.ventestatuskodeRepository,
        )

    test("hentAlleFagomraader skal returnere en liste av Fagomraade") {
        Db2Listener.dataSource.transaction { session ->
            session.update(queryOf("database/fastedata/getFagomraader.sql".readFromResource())) shouldBeGreaterThan 0
        }

        val result = fastedataService.getFagomraader()
        result.shouldNotBeEmpty()
        result.size shouldBe 293

        val fagomraade = result.first()
        fagomraade.kodeFagomraade shouldBe "AAP"
        fagomraade.navnFagomraade shouldBe "Arbeidsavklaringspenger"
        fagomraade.kodeFaggruppe shouldBe "ARBYT"
        fagomraade.antAttestanter shouldBe 1
        fagomraade.maksAktOppdrag shouldBe 99
        fagomraade.tpsDistribusjon shouldBe "J"
        fagomraade.sjekkOffId shouldBe "J"
        fagomraade.anviser shouldBe "N"
        fagomraade.sjekkMotTps shouldBe "J"
        fagomraade.kodeMotregningsgruppe shouldBe "MAAP"
        fagomraade.korraarsakFinnes shouldBe false
        fagomraade.bilagstypeFinnes shouldBe false
        fagomraade.klassekodeFinnes shouldBe false
        fagomraade.regelFinnes shouldBe true
    }

    test("getAllVentekriterier skal returnere en liste av Ventekriterier") {
        Db2Listener.dataSource.transaction { session ->
            session.update(queryOf("database/fastedata/getVentekriterier.sql".readFromResource())) shouldBeGreaterThan 0
        }

        val result = fastedataService.getAllVentekriterier()
        result.shouldNotBeEmpty()
        result.size shouldBe 2

        val ventekriterier = result.first()
        ventekriterier.kodeFaggruppe shouldBe "GH"
        ventekriterier.typeBilag shouldBe "O"
        ventekriterier.datoFom shouldBe "1900-01-01"
        ventekriterier.belopBrutto shouldBe null
        ventekriterier.belopNetto shouldBe 100000.00
        ventekriterier.antDagerEldreenn shouldBe null
        ventekriterier.tidligereAar shouldBe false
    }

    test("getAllVentestatuskoder skal returnere en liste av Ventestatuskode") {
        Db2Listener.dataSource.transaction { session ->
            session.update(queryOf("database/fastedata/getVentestatuskoder.sql".readFromResource())) shouldBeGreaterThan 0
        }

        every { Db2Listener.ventestatuskodeRepository.getAllVentestatuskoder() }.returns(
            using(sessionOf(Db2Listener.dataSource)) { session ->
                session.list(
                    queryOf(
                        """
                        SELECT
                            v.KODE_VENTESTATUS AS KODE_VENTESTATUS,
                            v.BESKRIVELSE AS BESKRIVELSE,
                            v.TYPE_VENTESTATUS AS TYPE_VENTESTATUS,
                            v.KODE_ARVES_TIL AS KODE_ARVES_TIL,
                            v.SETTES_MANUELT AS SETTES_MANUELT,
                            v.OVERFOR_MOTTKOMP AS OVERFOR_MOTTKOMP,
                            v.PRIORITET AS PRIORITET,
                            COALESCE((SELECT
                                GROUP_CONCAT(KODE_VENTESTATUS_U SEPARATOR ', ')
                             FROM T_VENT_STATUSREGEL
                             WHERE KODE_VENTESTATUS_H = v.KODE_VENTESTATUS), '') AS KAN_MANUELT_ENDRES_TIL
                        FROM T_VENT_STATUSKODE v
                        ORDER BY v.KODE_VENTESTATUS
                        """.trimIndent(),
                    ),
                ) { row ->
                    Ventestatuskode(
                        kodeVentestatus = row.string("KODE_VENTESTATUS"),
                        beskrivelse = row.string("BESKRIVELSE"),
                        prioritet = row.intOrNull("PRIORITET"),
                        settesManuelt = row.string("SETTES_MANUELT"),
                        kodeArvesTil = row.stringOrNull("KODE_ARVES_TIL"),
                        kanManueltEndresTil = row.stringOrNull("KAN_MANUELT_ENDRES_TIL"),
                    )
                }
            },
        )

        val result = fastedataService.getAllVentestatuskoder()
        result.shouldNotBeEmpty()
        result.size shouldBe 2

        val ventestatuskode = result.first()
        ventestatuskode.kodeVentestatus shouldBe "ADDR"
        ventestatuskode.beskrivelse shouldBe "Periode ikke utbet, navn/adresse mangler"
        ventestatuskode.prioritet shouldBe 120
        ventestatuskode.settesManuelt shouldBe "J"
        ventestatuskode.kodeArvesTil shouldBe "ADDR"
        ventestatuskode.kanManueltEndresTil shouldBe "AVVE"
    }
})
