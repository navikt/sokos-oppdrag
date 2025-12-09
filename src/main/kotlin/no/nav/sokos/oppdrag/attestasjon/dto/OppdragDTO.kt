package no.nav.sokos.oppdrag.attestasjon.dto

import kotlinx.serialization.Serializable

@Serializable
data class OppdragDTO(
    val ansvarssted: String? = null,
    val antAttestanter: Int,
    val navnFaggruppe: String,
    val navnFagomraade: String,
    val fagSystemId: String,
    val oppdragGjelderId: String,
    val kodeFaggruppe: String,
    val kodeFagomraade: String,
    val kostnadssted: String,
    val oppdragsId: Int,
    val erSkjermetForSaksbehandler: Boolean,
    val hasWriteAccess: Boolean,
    val typeBilag: String? = null,
)
