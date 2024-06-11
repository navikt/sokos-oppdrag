package no.nav.sokos.oppdrag.oppdragsinfo.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsInfoRequest(
    val gjelderId: String,
    val fagGruppeKode: String? = null,
)
