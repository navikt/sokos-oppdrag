package no.nav.sokos.oppdrag.fastedata.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotliquery.queryOf

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.listener.Db2Listener
import no.nav.sokos.oppdrag.listener.Db2Listener.fasteDataFagomraadeRepository
import no.nav.sokos.oppdrag.listener.Db2Listener.venteKriterierRepository

internal class FasteDataServiceTest :
    FunSpec({
        extensions(Db2Listener)

        val fastedataService =
            FasteDataService(
                fasteDataFagomraadeRepository,
                venteKriterierRepository,
            )

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
    })
