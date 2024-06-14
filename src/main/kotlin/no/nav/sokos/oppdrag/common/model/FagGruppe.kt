package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class FagGruppe(
    val navn: String,
    val type: String,
)
