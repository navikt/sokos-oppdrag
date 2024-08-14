package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val ansvarsSted: String,
    val fagsystemId: String,
    val gjelderId: String,
    val kostnadsSted: String,
    val navnFagGruppe: String,
    val navnFagOmraade: String,
    val oppdragsId: Int,
)
