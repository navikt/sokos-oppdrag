package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ventestatuskode(
    val kodeVentestatus: String,
    val beskrivelse: String,
    val prioritet: Int?,
    val settesManuelt: Boolean,
    val kodeArvesTil: String?,
    val kanManueltEndresTil: String?,
)
