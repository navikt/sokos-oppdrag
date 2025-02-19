package no.nav.sokos.oppdrag.fastedata

import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier

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
