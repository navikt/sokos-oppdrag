package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ventestatuskode(
    val kodeVenteStatus: String,
    val beskrivelse: String,
    val typeVenteStatus: String?,
    val kodeArvesTil: String?,
    val settesManuelt: Boolean,
    val overforMottKomp: Boolean,
    val prioritet: Int?,
    val kanManueltEndresTil: List<String>,
)
