package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsIdRequest(
    val oppdragsIder: List<Int>,
)
