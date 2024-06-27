package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class SokAttestasjonRequestBody(
    val gjelderId: String?,
    val fagsystemId: String?,
    val kodeFaggruppe: String?,
    val kodeFagomraade: String?,
    val attestert: Boolean?,
)
