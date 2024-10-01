package no.nav.sokos.oppdrag.attestasjon.dto

import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje

@Serializable
data class OppdragslinjeDTO(
    val oppdragsLinje: Oppdragslinje,
    val ansvarsStedForOppdragsLinje: String?,
    val kostnadsStedForOppdragsLinje: String?,
    val attestasjoner: List<Attestasjon>,
)
