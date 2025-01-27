package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import io.mockk.coEvery
import io.mockk.mockk
import kotliquery.queryOf
import org.junit.jupiter.api.assertThrows

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.Testdata.navIdent
import no.nav.sokos.oppdrag.config.transaction
import no.nav.sokos.oppdrag.integration.service.SkjermingService
import no.nav.sokos.oppdrag.listener.Db2Listener
import no.nav.sokos.oppdrag.listener.Db2Listener.faggruppeRepository
import no.nav.sokos.oppdrag.listener.Db2Listener.oppdragRepository
import no.nav.sokos.oppdrag.listener.Db2Listener.oppdragsdetaljerRepository
import no.nav.sokos.oppdrag.oppdragsinfo.exception.OppdragsinfoException

private const val GJELDER_ID = "24029428499"
private const val OPPDRAGSID = 964801

internal class OppdragsInfoServiceTest :
    FunSpec({
        extensions(Db2Listener)

        val skjermingService = mockk<SkjermingService>()
        val oppdragsInfoService =
            OppdragsInfoService(
                oppdragRepository,
                oppdragsdetaljerRepository,
                faggruppeRepository,
                skjermingService,
            )

        beforeEach {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }
        }

        test("getFagGrupper skal returnere navn og type") {
            val result = faggruppeRepository.getFagGrupper()
            result.forEach { fagGruppe ->
                fagGruppe.navn shouldNotBe null
                fagGruppe.type shouldNotBe null
            }
        }

        test("getOppdrag med saksbehandler har tilgang til personen") {
            coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns false
            val result = oppdragsInfoService.getOppdrag(GJELDER_ID, "", navIdent)

            result.shouldNotBeEmpty()
            result.size shouldBe 10
        }

        test("getOppdrag skal kaste exception hvis saksbehandler ikke har tilgang til personen") {
            coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns true
            assertThrows<OppdragsinfoException> {
                oppdragsInfoService.getOppdrag(GJELDER_ID, "", navIdent)
            }
        }

        test("getOppdrag skal returnere tom liste hvis ingen oppdrager finnes") {
            coEvery { skjermingService.getSkjermingForIdent(any(), any()) } returns false
            val result = oppdragsInfoService.getOppdrag("12345678901", "", navIdent)

            result.shouldBeEmpty()
        }

        test("getBehandlendeEnhetForOppdrag skal returnere en OppdragsEnhetDTO") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getBehandlendeEnhetForOppdrag.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getBehandlendeEnhetForOppdrag(OPPDRAGSID)
            result shouldNotBe null
            result.enhet.type shouldNotBe "BEH"
            result.behandlendeEnhet?.type shouldBe "BEH"
        }

        test("getOppdragsLinjer skal returnere en liste av OppdragsLinje") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjer.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjer(OPPDRAGSID)
            result.shouldNotBeEmpty()
            result.size shouldBe 10

            val oppdragsLinje = result.first()
            oppdragsLinje.linjeId shouldBe 1
            oppdragsLinje.kodeKlasse shouldBe "PENBPGP-OPTP"
            oppdragsLinje.datoVedtakFom shouldBe "2009-01-01"
            oppdragsLinje.datoVedtakTom shouldBe null
            oppdragsLinje.sats shouldBe 1756.0
            oppdragsLinje.typeSats shouldBe "MND"
            oppdragsLinje.kodeStatus shouldBe "KORR"
            oppdragsLinje.datoFom shouldBe "2009-05-01"
            oppdragsLinje.linjeIdKorr shouldBe 2
            oppdragsLinje.attestert shouldBe "J"
            oppdragsLinje.delytelseId shouldBe "12249330"
            oppdragsLinje.utbetalesTilId shouldBe "01010093666"
            oppdragsLinje.refunderesOrgnr.shouldBeEmpty()
            oppdragsLinje.brukerId shouldBe "KONV"
            oppdragsLinje.tidspktReg shouldBe "2008-12-06 12:29:45.435239"
        }

        test("getOppdragsOmposteringer skal returnere en liste av Ompostering") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsOmposteringer.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsOmposteringer(73195442)
            result.shouldNotBeEmpty()
            result.size shouldBe 2

            val ompostering = result.first()
            ompostering.id shouldBe "01015949240"
            ompostering.kodeFaggruppe shouldBe "KORTTID"
            ompostering.lopenr shouldBe 1
            ompostering.ompostering shouldBe "J"
            ompostering.omposteringFom shouldBe "2024-06-16"
            ompostering.feilReg shouldBe "N"
            ompostering.beregningsId shouldBe 558151804
            ompostering.utfort shouldBe "N"
            ompostering.brukerid shouldBe "K231B222"
            ompostering.tidspktReg shouldBe "2024-07-12 22:43:17.793295"
        }

        test("getOppdragsEnhetsHistorikk skal returnere en liste av OppdragsEnhet") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsEnhetsHistorikk.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsEnhetsHistorikk(OPPDRAGSID.toString())
            result.shouldNotBeEmpty()
            result.size shouldBe 4

            val oppdragsEnhet = result.first()
            oppdragsEnhet.type shouldBe "BEH"
            oppdragsEnhet.datoFom shouldBe "2008-12-06"
            oppdragsEnhet.enhet shouldBe "4819"
        }

        test("getOppdragsStatusHistorikk skal returnere en liste av OppdragsStatus") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsStatusHistorikk.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsStatusHistorikk(OPPDRAGSID.toString())
            result.shouldNotBeEmpty()
            result.size shouldBe 2

            val oppdragsStatus = result.first()
            oppdragsStatus.kodeStatus shouldBe "AKTI"
            oppdragsStatus.tidspktReg shouldBe "2008-12-06 12:29:45.520157"
            oppdragsStatus.brukerid shouldBe "KONV"
        }

        test("getOppdragsLinjeStatuser skal returnere en liste av LinjeStatus") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeStatuser.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeStatuser(OPPDRAGSID.toString(), "1")
            result.shouldNotBeEmpty()
            result.size shouldBe 3

            val linjeStatus = result.first()
            linjeStatus.status shouldBe "LOPE"
            linjeStatus.datoFom shouldBe "2009-01-01"
            linjeStatus.tidspktReg shouldBe "2009-01-03 21:29:52.396564"
            linjeStatus.brukerid shouldBe "K231B260"
        }

        test("getOppdragsLinjeAttestanter skal returnere en liste av Attestant") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeAttestanter.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeAttestanter(OPPDRAGSID.toString(), "1")
            result.shouldNotBeEmpty()
            result.size shouldBe 1

            val attestant = result.first()
            attestant.attestantId shouldBe "KONV"
            attestant.ugyldigFom shouldBe "9999-12-31"
        }

        test("getOppdragsLinjeDetaljer skal returnere en OppdragsLinjeDetaljerDTO") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjer.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeDetaljer.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val oppdragsLinjeDetaljer = oppdragsInfoService.getOppdragsLinjeDetaljer(OPPDRAGSID.toString(), "1")
            oppdragsLinjeDetaljer shouldNotBe null
            oppdragsLinjeDetaljer.korrigerteLinjeIder?.size shouldBe 10
            val oppdragsLinje = oppdragsLinjeDetaljer.korrigerteLinjeIder!!.first()
            oppdragsLinje.linjeId shouldBe 1
            oppdragsLinje.kodeKlasse shouldBe "PENBPGP-OPTP"
            oppdragsLinje.datoVedtakFom shouldBe "2009-01-01"
            oppdragsLinje.datoVedtakTom shouldBe null
            oppdragsLinje.sats shouldBe 1756.0
            oppdragsLinje.typeSats shouldBe "MND"
            oppdragsLinje.kodeStatus shouldBe "KORR"
            oppdragsLinje.datoFom shouldBe "2009-05-01"
            oppdragsLinje.linjeIdKorr shouldBe 2
            oppdragsLinje.attestert shouldBe "J"
            oppdragsLinje.delytelseId shouldBe "12249330"
            oppdragsLinje.utbetalesTilId shouldBe "01010093666"
            oppdragsLinje.refunderesOrgnr.shouldBeEmpty()
            oppdragsLinje.brukerId shouldBe "KONV"
            oppdragsLinje.tidspktReg shouldBe "2008-12-06 12:29:45.435239"

            oppdragsLinjeDetaljer.harValutaer shouldBe false
            oppdragsLinjeDetaljer.harSkyldnere shouldBe true
            oppdragsLinjeDetaljer.harKravhavere shouldBe false
            oppdragsLinjeDetaljer.harEnheter shouldBe false
            oppdragsLinjeDetaljer.harGrader shouldBe false
            oppdragsLinjeDetaljer.harTekster shouldBe false
            oppdragsLinjeDetaljer.harKidliste shouldBe false
            oppdragsLinjeDetaljer.harMaksdatoer shouldBe false
        }

        test("getOppdragsLinjeValutaer skal returnere en liste av Valuta") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeValutaer.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeValutaer(OPPDRAGSID.toString(), "1")
            result.size shouldBe 5

            val valuta = result.first()
            valuta.linjeId shouldBe 1
            valuta.type shouldBe "UTB"
            valuta.datoFom shouldBe "2002-03-01"
            valuta.nokkelId shouldBe 1
            valuta.valuta shouldBe "SEK"
            valuta.feilreg shouldBe " "
            valuta.tidspktReg shouldBe "2006-03-27 07:28:37.786667"
            valuta.brukerid shouldBe "DKF028"
        }

        test("getOppdragsLinjeSkyldnere skal returnere en liste av Skyldner") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeSkyldnere.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeSkyldnere(OPPDRAGSID.toString(), "1")
            result.size shouldBe 5

            val skyldner = result.first()
            skyldner.linjeId shouldBe 1
            skyldner.skyldnerId shouldBe "14045920284"
            skyldner.datoFom shouldBe "2002-03-01"
            skyldner.tidspktReg shouldBe "2006-03-27 07:28:37.718281"
            skyldner.brukerid shouldBe "DKF028"
        }

        test("getOppdragsLinjeKravhavere skal returnere en liste av Kravhaver") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeKravhavere.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeKravhavere(OPPDRAGSID.toString(), "1")
            result.size shouldBe 5

            val kravhaver = result.first()
            kravhaver.linjeId shouldBe 1
            kravhaver.kravhaverId shouldBe "06239000116"
            kravhaver.datoFom shouldBe "2002-03-01"
            kravhaver.tidspktReg shouldBe "2006-03-27 07:28:37.718482"
            kravhaver.brukerid shouldBe "DKF028"
        }

        test("getOppdragsLinjeEnheter skal returnere en liste av Enheter") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeEnheter.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeEnheter(OPPDRAGSID.toString(), "1")
            result.size shouldBe 1

            val enhet = result.first()
            enhet.linjeId shouldBe 1
            enhet.typeEnhet shouldBe "BEH"
            enhet.enhet shouldBe "0326"
            enhet.datoFom shouldBe "2007-01-01"
            enhet.nokkelId shouldBe 1
            enhet.tidspktReg shouldBe "2006-11-23 15:29:30.762657"
            enhet.brukerid shouldBe "SJA0326"
        }

        test("getOppdragsLinjeGrader skal returnere en liste av Grader") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeGrader.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeGrader(OPPDRAGSID.toString(), "1")
            result.size shouldBe 5

            val grad = result.first()
            grad.linjeId shouldBe 1
            grad.typeGrad shouldBe "UFOR"
            grad.grad shouldBe 100
            grad.tidspktReg shouldBe "2004-01-21 14:12:31.104901"
            grad.brukerid shouldBe "GUA0906"
        }

        test("getOppdragsLinjeTekster skal returnere en liste av Tekster") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeTekster.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeTekster(OPPDRAGSID.toString(), "1")
            result.size shouldBe 1

            val tekst = result.first()
            tekst.linjeId shouldBe 1
            tekst.tekst shouldBe "null null                               "
        }

        test("getOppdragsLinjeKidListe skal returnere en liste av KidListe") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeKidListe.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeKid(OPPDRAGSID.toString(), "1")
            result.size shouldBe 5

            val kidListe = result.first()
            kidListe.linjeId shouldBe 1
            kidListe.kid shouldBe "02892958907763100300007"
            kidListe.datoFom shouldBe "2016-07-30"
            kidListe.tidspktReg shouldBe "2016-07-30 01:31:50.560693"
            kidListe.brukerid shouldBe "K231B708"
        }

        test("getOppdragsLinjeMaksDatoer skal returnere en liste av MaksDato") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeMaksDatoer.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeMaksDatoer(OPPDRAGSID.toString(), "1")
            result.size shouldBe 2

            val maksDato = result.first()
            maksDato.linjeId shouldBe 1
            maksDato.maksdato shouldBe "2017-11-03"
            maksDato.datoFom shouldBe "2016-11-10"
            maksDato.tidspktReg shouldBe "2016-11-26 19:43:29.450234"
            maksDato.brukerid shouldBe "GMA1149"
        }

        test("getOppdragsLinjeOvriger skal returnere en liste av Ovriger") {
            Db2Listener.dataSource.transaction { session ->
                session.update(queryOf("database/oppdragsinfo/getKorreksjoner.sql".readFromResource())) shouldBeGreaterThan 0
                session.update(queryOf("database/oppdragsinfo/getOppdragsLinjeOvriger.sql".readFromResource())) shouldBeGreaterThan 0
            }

            val result = oppdragsInfoService.getOppdragsLinjeOvriger(OPPDRAGSID.toString(), "1")
            result.size shouldBe 11

            val ovriger = result.first()
            ovriger.linjeId shouldBe 1
            ovriger.vedtaksId shouldBe "20090101"
            ovriger.henvisning shouldBe "11008824"
            ovriger.soknadsType shouldBe "NY"
        }
    })
