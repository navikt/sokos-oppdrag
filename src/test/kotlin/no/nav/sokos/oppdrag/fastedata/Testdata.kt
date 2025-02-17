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
            datoFom = "01.12.2024",
            belopBrutto = "300 000,00",
            belopNetto = "250 000,00",
            antDagerEldreenn = 30,
            tidligereAar = true,
        ),
        Ventekriterier(
            kodeFaggruppe = "AAP",
            typeBilag = "N",
            datoFom = "05.03.2023",
            belopBrutto = "100 000,00",
            belopNetto = "80 000,00",
            antDagerEldreenn = 60,
            tidligereAar = false,
        ),
    )
