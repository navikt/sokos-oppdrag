package no.nav.sokos.oppdrag.oppdragsinfo.dto

import kotlinx.serialization.Serializable

import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinje

@Serializable
data class OppdragsLinjeDetaljerDTO(
    val korrigerteLinjeIder: List<OppdragsLinje>? = null,
    val harValutaer: Boolean,
    val harSkyldnere: Boolean,
    val harKravhavere: Boolean,
    val harEnheter: Boolean,
    val harGrader: Boolean,
    val harTekster: Boolean,
    val harKidliste: Boolean,
    val harMaksdatoer: Boolean,
)
