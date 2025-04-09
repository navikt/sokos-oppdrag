package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Attestant(
    val attestantId: String,
    val datoUgyldigFom: String,
)
