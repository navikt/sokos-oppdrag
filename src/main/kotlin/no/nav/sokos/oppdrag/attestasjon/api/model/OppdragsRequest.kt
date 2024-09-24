package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsRequest(
    val gjelderId: String? = null,
    val fagsystemId: String? = null,
    val kodeFaggruppe: String? = null,
    val kodeFagomraade: String? = null,
    val attestert: Boolean? = null,
)
