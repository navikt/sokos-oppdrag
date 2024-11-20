package no.nav.sokos.oppdrag.attestasjon.dto

import kotlinx.serialization.Serializable

@Serializable
data class OppdragDTO(
    val ansvarsSted: String? = null,
    val antallAttestanter: Int,
    val fagGruppe: String,
    val fagOmraade: String,
    val fagSystemId: String,
    val gjelderId: String,
    val kodeFagGruppe: String,
    val kodeFagOmraade: String,
    val kostnadsSted: String,
    val oppdragsId: Int,
    val erSkjermetForSaksbehandler: Boolean,
    val hasWriteAccess: Boolean,
)
