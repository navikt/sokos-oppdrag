package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Trekkgruppe(
    val kodeTrekkgruppe: String,
    val kodeFagomraade: String,
)
