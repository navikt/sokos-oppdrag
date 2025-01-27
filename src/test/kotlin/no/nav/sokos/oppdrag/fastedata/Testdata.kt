package no.nav.sokos.oppdrag.fastedata

import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak

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
            anviser = "N  ",
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
