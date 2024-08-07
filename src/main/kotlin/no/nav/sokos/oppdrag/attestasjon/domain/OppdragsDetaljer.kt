package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val klasse: String,
    val delytelsesId: String,
    val sats: Double,
    val satstype: String,
    val datoVedtakFom: String,
    val datoVedtakTom: String?,
    val attestant: String? = null,
    val navnFagOmraade: String,
    val fagsystemId: String,
)
