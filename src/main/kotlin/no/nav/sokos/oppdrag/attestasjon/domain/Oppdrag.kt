package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val ansvarssted: String? = null,
    val antallAttestanter: Int,
    val faggruppe: String,
    val fagomraade: String,
    val fagsystemId: String,
    val gjelderId: String,
    val kodeFaggruppe: String,
    val kodeFagomraade: String,
    val kostnadssted: String,
    val oppdragsId: Int,
)
