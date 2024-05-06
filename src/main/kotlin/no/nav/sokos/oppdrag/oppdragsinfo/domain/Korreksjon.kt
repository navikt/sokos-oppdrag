package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Korreksjon(
    val linje: Int,
    val korrigertLinje: Int,
)
