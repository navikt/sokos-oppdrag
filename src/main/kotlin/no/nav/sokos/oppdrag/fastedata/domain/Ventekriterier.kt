package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ventekriterier(
    val kodeFaggruppe: String,
    val typeBilag: String,
    val datoFom: String,
    val belopBrutto: Double?,
    val belopNetto: Double?,
    val antDagerEldreenn: Int?,
    val tidligereAar: Boolean?,
)
