package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val ansvarsSted: String? = null,
    val fagSystemId: String,
    val gjelderId: String,
    val kostnadsSted: String,
    val fagGruppe: String,
    val fagOmraade: String,
    val oppdragsId: Int,
)
