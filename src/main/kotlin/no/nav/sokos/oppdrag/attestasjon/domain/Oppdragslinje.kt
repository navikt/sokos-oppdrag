package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdragslinje(
    val oppdragsLinje: OppdragslinjePlain,
    val ansvarsstedForOppdragsLinje: String?,
    val kostnadsstedForOppdragsLinje: String?,
    val attestasjoner: List<Attestasjon>,
)
