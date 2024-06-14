package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SokOppdragRequestBody(
    val gjelderId: String,
    val fagGruppeKode: String? = null,
)
