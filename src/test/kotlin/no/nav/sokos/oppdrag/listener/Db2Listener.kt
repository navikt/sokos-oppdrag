package no.nav.sokos.oppdrag.listener

import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldNotBe
import io.mockk.spyk
import kotliquery.queryOf

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.config.DatabaseTestConfig
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.fastedata.repository.FaggruppeRepository
import no.nav.sokos.oppdrag.fastedata.repository.FagomraadeRepository
import no.nav.sokos.oppdrag.fastedata.repository.KjoreplanRepository
import no.nav.sokos.oppdrag.fastedata.repository.KlassekoderRepository
import no.nav.sokos.oppdrag.fastedata.repository.VentekriterierRepository
import no.nav.sokos.oppdrag.fastedata.repository.VentestatuskodeRepository
import no.nav.sokos.oppdrag.kodeverk.repository.KodeverkRepository
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragRepository
import no.nav.sokos.oppdrag.oppdragsinfo.repository.OppdragsdetaljerRepository

object Db2Listener : TestListener {
    val dataSource = HikariDataSource(DatabaseTestConfig.hikariConfig())
    val attestasjonRepository = spyk(AttestasjonRepository(dataSource))
    val faggruppeRepository = spyk(FaggruppeRepository(dataSource))
    val oppdragRepository = spyk(OppdragRepository(dataSource))
    val oppdragsdetaljerRepository = spyk(OppdragsdetaljerRepository(dataSource))
    val kodeverkRepository = spyk(KodeverkRepository(dataSource))
    val venteKriterierRepository = spyk(VentekriterierRepository(dataSource))
    val fasteDataFagomraadeRepository = spyk(FagomraadeRepository(dataSource))
    val ventestatuskodeRepository = spyk(VentestatuskodeRepository(dataSource))
    val klassekoderRepository = spyk(KlassekoderRepository(dataSource))
    val kjoreplanRepository = spyk(KjoreplanRepository(dataSource))

    override suspend fun beforeSpec(spec: Spec) {
        dataSource shouldNotBe null
        attestasjonRepository shouldNotBe null
        oppdragRepository shouldNotBe null
        oppdragsdetaljerRepository shouldNotBe null
        kodeverkRepository shouldNotBe null
        ventestatuskodeRepository shouldNotBe null

        dataSource.transaction { session ->
            val query = "database/db2Script.sql".readFromResource()
            session.update(queryOf(query))
        }
    }

    override suspend fun afterEach(
        testCase: TestCase,
        result: TestResult,
    ) {
        resetDatabase()
    }

    private fun resetDatabase() {
        dataSource.transaction { session ->
            session.update(queryOf("DELETE FROM T_ATTESTASJON"))
            session.update(queryOf("DELETE FROM T_GRAD"))
            session.update(queryOf("DELETE FROM T_KID"))
            session.update(queryOf("DELETE FROM T_KJOREDATO"))
            session.update(queryOf("DELETE FROM T_KONTOREGEL"))
            session.update(queryOf("DELETE FROM T_KORREKSJON"))
            session.update(queryOf("DELETE FROM T_KRAVHAVER"))
            session.update(queryOf("DELETE FROM T_LINJEENHET"))
            session.update(queryOf("DELETE FROM T_LINJE_STATUS"))
            session.update(queryOf("DELETE FROM T_LINJE_VEDTAKSSATS"))
            session.update(queryOf("DELETE FROM T_MAKS_DATO"))
            session.update(queryOf("DELETE FROM T_OMPOSTERING"))
            session.update(queryOf("DELETE FROM T_OPPDRAG"))
            session.update(queryOf("DELETE FROM T_OPPDRAGSENHET"))
            session.update(queryOf("DELETE FROM T_OPPDRAGSLINJE"))
            session.update(queryOf("DELETE FROM T_OPPDRAG_STATUS"))
            session.update(queryOf("DELETE FROM T_SKYLDNER"))
            session.update(queryOf("DELETE FROM T_TEKST"))
            session.update(queryOf("DELETE FROM T_VALUTA"))
            session.update(queryOf("DELETE FROM T_VENT_KRITERIUM"))
        }
    }
}
