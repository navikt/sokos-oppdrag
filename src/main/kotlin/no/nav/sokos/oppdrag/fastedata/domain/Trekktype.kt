package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Trekktype(
    val kodeTrekktype: String,
    val beskrivelse: String,
    val prioritet: String,
    val reduserSkattegrl: String?,
    val kodeKlasseTrekk: String?,
    val kodeTrekkategori: String?,
    val typeTrekkberegning: String?,
)
