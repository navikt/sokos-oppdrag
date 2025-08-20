package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class RedusertSkatt(
    val kodeFaggruppe: String,
    val periode: String,
    val prosent: Int,
)
