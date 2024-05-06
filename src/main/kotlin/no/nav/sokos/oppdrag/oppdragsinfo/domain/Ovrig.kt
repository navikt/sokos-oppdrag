package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ovrig(
    val linjeId: Int,
    val vedtaksId: String,
    val henvisning: String,
    val soknadsType: String,
)
