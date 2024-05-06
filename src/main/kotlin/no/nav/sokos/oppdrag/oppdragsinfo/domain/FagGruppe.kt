package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class FagGruppe(
    val navn: String,
    val type: String,
)
