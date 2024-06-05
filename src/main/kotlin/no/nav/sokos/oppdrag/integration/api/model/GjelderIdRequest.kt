package no.nav.sokos.oppdrag.integration.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GjelderIdRequest(
    val gjelderId: String,
    val fagGruppeKode: String? = null,
)
