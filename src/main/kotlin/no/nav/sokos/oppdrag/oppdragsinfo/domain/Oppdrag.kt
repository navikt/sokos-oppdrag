package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val kostnadssted: OppdragsEnhet,
    val ansvarssted: OppdragsEnhet? = null,
    val harOmposteringer: Boolean,
    val oppdragsLinjer: List<OppdragsLinje>,
)
