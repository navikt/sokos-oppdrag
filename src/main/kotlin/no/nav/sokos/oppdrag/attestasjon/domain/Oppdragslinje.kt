package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdragslinje(
    val oppdragsLinje: OppdragslinjePlain,
    val ansvarsStedForOppdragsLinje: String?,
    val kostnadsStedForOppdragsLinje: String?,
    val attestasjoner: List<Attestasjon>,
)
