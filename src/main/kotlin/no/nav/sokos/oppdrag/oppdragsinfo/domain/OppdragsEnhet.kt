package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsEnhet(
    val typeEnhet: String,
    val datoFom: String,
    val enhet: String,
)
