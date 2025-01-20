package no.nav.sokos.oppdrag.fastedata.dto

import kotlinx.serialization.Serializable

import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak

@Serializable
data class KorrigeringsaarsakDTO(
    val navn: String,
    val kode: String,
    val medforerKorrigering: Boolean,
) {
    constructor(korrigeringsaarsak: Korrigeringsaarsak) : this (
        navn = korrigeringsaarsak.beskrivelse,
        kode = korrigeringsaarsak.kodeAarsakKorrigering,
        medforerKorrigering = korrigeringsaarsak.medforerKorrigering,
    )
}
