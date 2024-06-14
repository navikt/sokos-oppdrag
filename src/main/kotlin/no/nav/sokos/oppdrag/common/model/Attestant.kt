package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Attestant(
    val attestantId: String,
    val ugyldigFom: String,
)
