package no.nav.sokos.oppdrag.fastedata

import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Faggruppe
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Klassekode
import no.nav.sokos.oppdrag.fastedata.domain.Klassekoder
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode

val fagomraader =
    listOf(
        Fagomraade(
            kodeFagomraade = "AAP",
            navnFagomraade = "Arbeidsavklaringspenger",
            kodeFaggruppe = "ARBYT",
            antAttestanter = 1,
            maksAktOppdrag = 99,
            tpsDistribusjon = "J",
            sjekkOffId = "J",
            anviser = "N",
            sjekkMotTps = "J",
            kodeMotregningsgruppe = "MAAP",
            bilagstypeFinnes = true,
            klassekodeFinnes = true,
            korraarsakFinnes = true,
            regelFinnes = true,
        ),
        Fagomraade(
            kodeFagomraade = "AAPARENA",
            navnFagomraade = "Arbeidsavklaringspenger",
            kodeFaggruppe = "ARBYT",
            antAttestanter = 1,
            maksAktOppdrag = 99,
            tpsDistribusjon = "J",
            sjekkOffId = "J",
            anviser = "N",
            sjekkMotTps = "J",
            kodeMotregningsgruppe = "MAAP",
            bilagstypeFinnes = false,
            klassekodeFinnes = false,
            korraarsakFinnes = false,
            regelFinnes = false,
        ),
    )

val korrigeringsaarsaker =
    listOf(
        Korrigeringsaarsak(
            beskrivelse = "Linjestatus endret",
            kodeAarsakKorr = "0001",
            medforerKorr = true,
        ),
    )
val bilagstype =
    listOf(
        Bilagstype(
            kodeFagomraade = "AAPARENA",
            typeBilag = "O",
            datoFom = "2024-12-01",
            datoTom = "2022-12-01",
            autoFagsystemId = "ABC",
        ),
    )

val klassekoder =
    listOf(
        Klassekode(
            kodeKlasse = "String",
        ),
    )

val alleKlassekoder =
    listOf(
        Klassekoder(
            kodeKlasse = "TPTPTILTAK",
            kodeFagomraade = "",
            artID = 54,
            datoFom = "2022-10-01",
            datoTom = "9999-12-31",
            hovedkontoNr = "776",
            underkontoNr = "1010",
            beskrKlasse = "Tiltakspenger tiltak",
            beskrArt = "Overføringer",
            hovedkontoNavn = "Arbeidskonto innbetaling",
            underkontoNavn = "Bankkonto 7694.05.12146",
        ),
        Klassekoder(
            kodeKlasse = "0301",
            kodeFagomraade = "",
            artID = 50,
            datoFom = "2003-01-01",
            datoTom = "2017-12-31",
            hovedkontoNr = "051",
            underkontoNr = "0301",
            beskrKlasse = "Skattetrekk",
            beskrArt = "Trekk",
            hovedkontoNavn = "Skatt",
            underkontoNavn = "Påleggstrekk skatt",
        ),
    )

val ventekriterier =
    listOf(
        Ventekriterier(
            kodeFaggruppe = "ARBTIL",
            typeBilag = "O",
            datoFom = "2024-12-01",
            belopBrutto = 300000.00,
            belopNetto = 250000.00,
            antDagerEldreenn = 30,
            tidligereAar = true,
        ),
        Ventekriterier(
            kodeFaggruppe = "AAP",
            typeBilag = "N",
            datoFom = "2023-03-05",
            belopBrutto = 300000.00,
            belopNetto = 250000.00,
            antDagerEldreenn = 60,
            tidligereAar = false,
        ),
    )

val ventestatuskoder =
    listOf(
        Ventestatuskode(
            kodeVentestatus = "ADDR",
            beskrivelse = "Periode ikke utbet, navn/adresse mangler",
            prioritet = 120,
            settesManuelt = "J",
            kodeArvesTil = "ADDR",
            kanManueltEndresTil = "AVVE, REAK, REBE, STOP",
        ),
        Ventestatuskode(
            kodeVentestatus = "AFFR",
            beskrivelse = "Midlertidig stopp av overf UR, arb.g.",
            prioritet = 35,
            settesManuelt = "N",
            kodeArvesTil = "AVAV",
            kanManueltEndresTil = "REAK, REBE",
        ),
    )

val faggrupper =
    listOf(
        Faggruppe(
            kodeFaggruppe = "BA",
            navnFaggruppe = "Barnetrygd",
            skatteprosent = 0,
            ventedager = 0,
            klassekodeFeil = "KL_KODE_FEIL_BA",
            klassekodeJustering = "KL_KODE_JUST_BA",
            klassekodeMotpFeil = "TBMOTOBS",
            klassekodeMotpTrekk = "TBTREKK",
            klassekodeMotpInnkr = "TBMOTFB",
            destinasjon = "OB01",
            reskontroOppdrag = "BA",
            onlineBeregning = true,
            pensjon = false,
            oereavrunding = true,
            samordnetBeregning = "F",
            prioritet = 99,
            antallFagomraader = 4,
            antallRedusertSkatt = 35,
            antallKjoreplaner = 720,
            nesteKjoredato = "2099-12-12",
        ),
        Faggruppe(
            kodeFaggruppe = "BIDRINKR",
            navnFaggruppe = "Bidragsreskontro",
            skatteprosent = 0,
            ventedager = 0,
            klassekodeFeil = "KL_KODE_FEIL",
            klassekodeJustering = "KL_KODE_JUST",
            klassekodeMotpFeil = "TBMOTOBS",
            klassekodeMotpTrekk = "TBTREKK",
            klassekodeMotpInnkr = "TBMOTFB",
            destinasjon = "OB01",
            reskontroOppdrag = "BRBATCH",
            onlineBeregning = true,
            pensjon = false,
            oereavrunding = true,
            samordnetBeregning = "O",
            prioritet = 99,
            antallFagomraader = 1,
            antallRedusertSkatt = 0,
            antallKjoreplaner = 70,
            nesteKjoredato = "2024-01-01",
        ),
    )

val alleKlassekoderMock =
    listOf(
        Klassekoder(
            kodeKlasse = "AAPAAPFAF",
            kodeFagomraade = "MTBBTARE,TBBTARE",
            artID = 51,
            datoFom = "2022-11-01",
            datoTom = "9999-12-31",
            hovedkontoNr = "341",
            underkontoNr = "2000",
            beskrKlasse = "Arb.avkl.penger ferdig attført",
            beskrArt = "Skatte-, trekk- og oppgavepliktig m/ompost",
            hovedkontoNavn = "Arbeidsavklaringspenger",
            underkontoNavn = "AAP",
        ),
        Klassekoder(
            kodeKlasse = "AAPAAPTFTK",
            kodeFagomraade = "MTBBTARE,TBBTARE",
            artID = 51,
            datoFom = "2022-11-01",
            datoTom = "9999-12-31",
            hovedkontoNr = "341",
            underkontoNr = "2400",
            beskrKlasse = "Arb.avkl.penger tilbakeføring til trygdekontor",
            beskrArt = "Skatte-, trekk- og oppgavepliktig m/ompost",
            hovedkontoNavn = "Arbeidsavklaringspenger",
            underkontoNavn = "AAP",
        ),
        Klassekoder(
            kodeKlasse = "AAPAAPUA",
            kodeFagomraade = "MAAP,MAAPAREN,MTBBTARE,TBBTARE",
            artID = 51,
            datoFom = "2022-11-01",
            datoTom = "9999-12-31",
            hovedkontoNr = "341",
            underkontoNr = "2200",
            beskrKlasse = "Arb.avkl.penger under attføring",
            beskrArt = "Skatte-, trekk- og oppgavepliktig m/ompost",
            hovedkontoNavn = "Arbeidsavklaringspenger",
            underkontoNavn = "AAP",
        ),
        Klassekoder(
            kodeKlasse = "AAPAAPUGFTILT",
            kodeFagomraade = "MTBBTARE,TBBTARE",
            artID = 51,
            datoFom = "2022-11-01",
            datoTom = "9999-12-31",
            hovedkontoNr = "341",
            underkontoNr = "2600",
            beskrKlasse = "Arb.avkl.penger gjennomføring av tiltak",
            beskrArt = "Skatte-, trekk- og oppgavepliktig m/ompost",
            hovedkontoNavn = "Arbeidsavklaringspenger",
            underkontoNavn = "AAP",
        ),
    )
