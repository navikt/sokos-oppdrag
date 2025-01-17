package no.nav.sokos.oppdrag.fastedata.dto

import kotlinx.serialization.Serializable

@Serializable
data class KorrigeringsaarsakDTO(
    val navn: String,
    val kode: String,
    val medforerKorrigering: Boolean,
)
