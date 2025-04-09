package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Korreksjon(
    val linjeId: Int,
    val linjeIdKorr: Int,
)
