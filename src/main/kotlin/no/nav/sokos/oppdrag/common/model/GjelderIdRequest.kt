package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class GjelderIdRequest(
    val gjelderId: String,
)
