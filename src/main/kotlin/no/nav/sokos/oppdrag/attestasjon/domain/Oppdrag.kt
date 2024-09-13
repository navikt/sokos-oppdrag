package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val ansvarsSted: String? = null,
    val fagsystemId: String,
    val gjelderId: String,
    val kostnadsSted: String,
    val fagGruppe: String,
    val kodeFagGruppe: String,
    val fagOmraade: String,
    val kodeFagOmraade: String,
    val oppdragsId: Int,
)
