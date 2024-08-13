package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val ansvarsSted: String,
    val antallAttestanter: Int,
    val attestant: String? = null,
    var datoUgyldigFom: String? = null,
    val datoVedtakFom: String,
    val datoVedtakTom: String? = null,
    val delytelsesId: String,
    val fagGruppeKode: String,
    val fagOmraadeKode: String,
    val fagSystemId: String,
    val klasse: String,
    val kostnadsSted: String,
    val linjeId: String,
    val oppdragGjelderId: String,
    val sats: Double,
    val satstype: String,
)
