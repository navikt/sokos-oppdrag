package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Korrigeringsaarsak(
    val navn: String,
    val kode: String,
    val medforerKorrigering: Boolean,
)
