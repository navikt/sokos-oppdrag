package no.nav.sokos.oppdrag.fastedata.service

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.fastedata.alleKlassekoderMock
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode
import no.nav.sokos.oppdrag.listener.Db2Listener

private const val KODE_FAGOMRAADE_MEFOGNY = "MEFOGNY"

internal class FasteDataServiceTest :
    FunSpec({
        extensions(Db2Listener)

        val fastedataService =
            FasteDataService(
                Db2Listener.fasteDataFagomraadeRepository,
                Db2Listener.faggruppeRepository,
                Db2Listener.venteKriterierRepository,
                Db2Listener.ventestatuskodeRepository,
                Db2Listener.klassekoderRepository,
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

        test("hentAlleFaggrupper skal returnere en liste av Faggruppe") {
            val result: List<Faggruppe> = fastedataService.getFaggrupper()
            result.shouldNotBeEmpty()
            result.size shouldBe 47

            assertSoftly(result.first()) {
                kodeFaggruppe shouldBe "BA"
                navnFaggruppe shouldBe "Barnetrygd"
                skatteprosent shouldBe 0
                ventedager shouldBe 0
                klassekodeFeil shouldBe "KL_KODE_FEIL_BA"
                klassekodeJustering shouldBe "KL_KODE_JUST_BA"
                destinasjon shouldBe "OB01"
                reskontroOppdrag shouldBe "BA"
                klassekodeMotpFeil shouldBe "TBMOTOBS"
                klassekodeMotpTrekk shouldBe "TBTREKK"
                klassekodeMotpInnkr shouldBe "TBMOTFB"
                prioritet shouldBe 99
                onlineBeregning shouldBe true
                pensjon shouldBe false
                oereavrunding shouldBe true
                samordnetBeregning shouldBe "F"
                antallFagomraader shouldBe 4
            }
        }

        test("getBilagstyper skal returnere en liste av Bilagstype for valgt fagområde") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/fastedata/getBilagstyper.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = fastedataService.getBilagstyper(KODE_FAGOMRAADE_MEFOGNY)
            result.shouldNotBeEmpty()
            result.size shouldBe 3

            val bilagstype = result.first()
            bilagstype.kodeFagomraade.trim() shouldBe KODE_FAGOMRAADE_MEFOGNY
            bilagstype.typeBilag shouldBe "MEMO"
            bilagstype.datoFom shouldBe "2013-01-01"
            bilagstype.datoTom shouldBe null
            bilagstype.autoFagsystemId shouldBe "N"
        }

        test("getKlassekoder skal returnere en liste av Klassekode for valgt fagområde") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/fastedata/getKlassekode.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = fastedataService.getKlassekoder(KODE_FAGOMRAADE_MEFOGNY)
            result.shouldNotBeEmpty()
            result.size shouldBe 7

            val klassekode = result.first()
            klassekode.kodeKlasse shouldBe "EFOGNYOR"
        }

        test("getKorrigeringsaarsaker skal returnere en liste av Korrigeringsaarsak for valgt fagområde") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/fastedata/getKorrigeringsaarsaker.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = fastedataService.getKorrigeringsaarsaker("BIDRINKR")
            result.shouldNotBeEmpty()
            result.size shouldBe 16

            val korrigeringsaarsak = result.first()
            korrigeringsaarsak.kodeAarsakKorr shouldBe "0001"
            korrigeringsaarsak.beskrivelse shouldBe "Linjestatus endret"
            korrigeringsaarsak.medforerKorr shouldBe false
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

        test("getFagomraaderForFaggruppe skal returnere listen av Fagområder som har den angitte Faggruppkoden") {
            val result = fastedataService.getFagomraaderForFaggruppe("ARBTIL")
            result.shouldNotBeEmpty()
            result.size shouldBe 2
            result shouldContain "TILLST"
            result shouldContain "TILTPENG"
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

        test("getAllKlassekoder skal returnere en liste av Klassekoder") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/fastedata/getKlassekoder.sql".readFromResource())) shouldBeGreaterThan 0
            }

            every { Db2Listener.klassekoderRepository.getAllKlassekoder() } returns alleKlassekoderMock

            val result = fastedataService.getAllKlassekoder()
            result.shouldNotBeEmpty()
            result.size shouldBe 4

            val klassekoder = result.first()
            klassekoder.kodeKlasse shouldBe "AAPAAPFAF"
            klassekoder.kodeFagomraade shouldBe "MTBBTARE,TBBTARE"
            klassekoder.artID shouldBe 51
            klassekoder.datoFom shouldBe "2022-11-01"
            klassekoder.datoTom shouldBe "9999-12-31"
            klassekoder.hovedkontoNr shouldBe "341"
            klassekoder.underkontoNr shouldBe "2000"
            klassekoder.beskrKlasse shouldBe "Arb.avkl.penger ferdig attført"
            klassekoder.beskrArt shouldBe "Skatte-, trekk- og oppgavepliktig m/ompost"
            klassekoder.hovedkontoNavn shouldBe "Arbeidsavklaringspenger"
            klassekoder.underkontoNavn shouldBe "AAP"
        }
    })
