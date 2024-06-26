package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjonTreff(
    val navnFaggruppe: String,
    val navnFagomraade: String,
    val oppdragsId: Int,
    val fagsystemId: String,
)
