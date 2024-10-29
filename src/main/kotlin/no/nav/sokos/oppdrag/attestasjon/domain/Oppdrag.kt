package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
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
    val skjermet: Boolean = false,
)
