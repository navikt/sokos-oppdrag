package no.nav.sokos.oppdrag.integration.tp

import kotlinx.serialization.Serializable

@Serializable
data class TpResponse(
    val navn: String,
)
