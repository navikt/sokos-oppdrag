package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class SokAttestasjonRequestBody(
    val gjelderId: String? = null,
    val fagsystemId: String? = null,
    val kodeFaggruppe: String? = null,
    val kodeFagomraade: String? = null,
    val attestert: Boolean? = null,
)
