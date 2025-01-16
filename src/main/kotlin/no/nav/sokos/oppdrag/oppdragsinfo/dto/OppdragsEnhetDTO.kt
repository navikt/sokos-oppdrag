package no.nav.sokos.oppdrag.oppdragsinfo.dto

import kotlinx.serialization.Serializable

import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet

@Serializable
data class OppdragsEnhetDTO(
    val enhet: OppdragsEnhet,
    val behandlendeEnhet: OppdragsEnhet? = null,
)
