package no.nav.sokos.oppdrag.fastedata

import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Klassekode
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
