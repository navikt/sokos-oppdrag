package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Fagomraade(
    val navn: String,
    val kode: String,
)
