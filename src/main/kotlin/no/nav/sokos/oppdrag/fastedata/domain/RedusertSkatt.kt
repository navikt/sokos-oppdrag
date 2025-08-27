package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class RedusertSkatt(
    val kodeFaggruppe: String,
    val datoFom: String,
    val datoTom: String,
    val prosent: Int,
)
