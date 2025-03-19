package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ventestatuskode(
    val kodeVentestatus: String,
    val beskrivelse: String,
    val prioritet: Int?,
    val settesManuelt: String,
    val kodeArvesTil: String?,
    val kanManueltEndresTil: String?,
)
