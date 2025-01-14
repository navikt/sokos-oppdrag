package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Fagomraade(
    val navn: String,
    val kode: String,
)
