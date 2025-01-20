package no.nav.sokos.oppdrag.fastedata

import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.dto.KorrigeringsaarsakDTO

val fagomraader =
    listOf(
        Fagomraade(
            antallAttestanter = 1,
            anviser = "lol",
            bilagstypeFinnes = true,
            klassekodeFinnes = true,
            kodeFagomraade = "lol",
            kodeFaggruppe = "lol",
            kodeMotregningsgruppe = "lol",
            korraarsakFinnes = true,
            maksAktiveOppdrag = 4,
            navnFagomraade = "lol",
            regelFinnes = true,
            sjekkMotTps = "lol",
            sjekkOffId = "lol",
            tpsDistribusjon = "lol",
        ),
        Fagomraade(
            antallAttestanter = 2,
            anviser = "test",
            bilagstypeFinnes = false,
            klassekodeFinnes = false,
            kodeFagomraade = "test",
            kodeFaggruppe = "test",
            kodeMotregningsgruppe = "test",
            korraarsakFinnes = false,
            maksAktiveOppdrag = 5,
            navnFagomraade = "test",
            regelFinnes = false,
            sjekkMotTps = "test",
            sjekkOffId = "test",
            tpsDistribusjon = "test",
        ),
        Fagomraade(
            antallAttestanter = 3,
            anviser = "example",
            bilagstypeFinnes = true,
            klassekodeFinnes = true,
            kodeFagomraade = "example",
            kodeFaggruppe = "example",
            kodeMotregningsgruppe = "example",
            korraarsakFinnes = true,
            maksAktiveOppdrag = 6,
            navnFagomraade = "example",
            regelFinnes = true,
            sjekkMotTps = "example",
            sjekkOffId = "example",
            tpsDistribusjon = "example",
        ),
    )

val korrigeringsaarsaker =
    listOf(
        Korrigeringsaarsak(
            beskrivelse = "Linjestatus endret",
            kodeAarsakKorrigering = "0001",
            medforerKorrigering = true,
        ),
    )

val korrigeringsaarsakDTOs =
    listOf(
        KorrigeringsaarsakDTO(
            navn = "Linjestatus endret",
            kode = "0001",
            medforerKorrigering = true,
        ),
    )
