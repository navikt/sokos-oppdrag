package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Korrigeringsaarsak(
    val beskrivelse: String,
    val kodeAarsakKorr: String,
    val medforerKorr: Boolean,
)
