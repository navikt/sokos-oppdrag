package no.nav.sokos.oppdrag.attestasjon.dto

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsdetaljerDTO(
    val linjer: List<OppdragslinjeDTO>,
    val saksbehandlerIdent: String,
)
