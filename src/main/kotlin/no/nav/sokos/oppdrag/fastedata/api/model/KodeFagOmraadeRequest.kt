package no.nav.sokos.oppdrag.fastedata.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KodeFagOmraadeRequest(
    val kodeFagOmraade: String,
)
