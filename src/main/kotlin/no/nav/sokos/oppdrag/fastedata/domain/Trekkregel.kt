package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Trekkregel(
    val kodeTrekktype: String,
    val beskrivelse: String,
    val prioritet: Int,
    val kodeKlasseTrekk: String,
    val kodeFagomraade: String,
    val antDagerOppf: Int?,
    val antDagerOppfUtf: Int?,
    val belopsgrense: Double,
    val oppfolging: String,
    val kodeOppgjorstype: String,
    val kodeOppgjorstypeNeg: String,
)
