package no.nav.sokos.oppdrag.oppdragsinfo.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SokOppdragRequest(
    val gjelderId: String,
    val fagGruppeKode: String? = null,
)

@Serializable
data class GjelderIdRequest(
    val gjelderId: String,
)
