package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val ansvarsStedForOppdrag: String? = null,
    val antallAttestanter: Int,
    val fagGruppe: String,
    val fagOmraade: String,
    val fagSystemId: String,
    val gjelderId: String,
    val kodeFagOmraade: String,
    val kostnadsStedForOppdrag: String,
    val oppdragsId: String,
    val linjer: List<Oppdragslinje>,
    val saksbehandlerIdent: String,
)
