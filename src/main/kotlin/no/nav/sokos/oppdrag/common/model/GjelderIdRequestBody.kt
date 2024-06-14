package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class GjelderIdRequestBody(
    val gjelderId: String,
)
