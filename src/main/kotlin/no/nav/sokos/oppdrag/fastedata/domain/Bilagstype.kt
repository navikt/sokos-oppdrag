package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Bilagstype(
    val kode: String,
    val type: String,
    val datoFom: String,
    val datoTom: String,
    val autoFagsystem: String,
)
