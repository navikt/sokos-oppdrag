package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val ansvarsstedForOppdrag: String? = null,
    val antallAttestanter: Int,
    val faggruppe: String,
    val fagomraade: String,
    val fagsystemId: String,
    val gjelderId: String,
    val kodeFagomraade: String,
    val kostnadsstedForOppdrag: String,
    val oppdragsId: String,
    val linjer: List<Oppdragslinje>,
    val saksbehandlerIdent: String,
)
