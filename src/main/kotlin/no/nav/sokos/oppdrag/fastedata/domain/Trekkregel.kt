package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Trekkregel(
    val kodeTrekktype: String,
    val beskrivelse: String,
    val prioritet: Int,
    val reduserSkattegr: String,
    val kodeKlasseTrekk: String,
    val typeTrekkberegning: String?,
    val kodeFagomraade: String,
    val antDagerOppf: Int?,
    val antDagerOppfUtf: Int?,
    val belopsgrense: Double,
    val oppfolging: String,
    val kodeBehandling: String,
    val kodeOppgjorstype: String,
    val kodeOppgjorstypeNeg: String,
    val brukerId: String,
)
