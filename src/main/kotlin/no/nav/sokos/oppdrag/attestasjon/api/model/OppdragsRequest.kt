package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsRequest(
    val gjelderId: String? = null,
    val fagSystemId: String? = null,
    val kodeFagGruppe: String? = null,
    val kodeFagOmraade: String? = null,
    val attestert: Boolean? = null,
    val lastOppdragsId: Int? = null,
)
