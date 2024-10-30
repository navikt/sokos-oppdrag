package no.nav.sokos.oppdrag.integration.client.tp

import kotlinx.serialization.Serializable

@Serializable
data class TpResponse(
    val navn: String,
)
