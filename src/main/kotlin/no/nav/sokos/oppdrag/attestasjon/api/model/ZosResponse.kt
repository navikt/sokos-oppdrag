package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ZosResponse(
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
