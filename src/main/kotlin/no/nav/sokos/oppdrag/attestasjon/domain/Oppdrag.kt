package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val gjelderId: String,
    val navnFagGruppe: String,
    val navnFagOmraade: String,
    val oppdragsId: Int,
    val fagsystemId: String,
)
