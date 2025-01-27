package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class FagOmraade(
    val navnFagomraade: String,
    val kodeFagomraade: String,
)
