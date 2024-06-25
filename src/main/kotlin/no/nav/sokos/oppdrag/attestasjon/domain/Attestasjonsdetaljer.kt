package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Attestasjonsdetaljer(
    val klasse: String,
    val delytelsesId: String,
    val sats: Double,
    val satstype: String,
    val datoVedtakFom: String,
    val datoVedtakTom: String?,
    val attestert: String,
    val attestant: String? = null,
    val navnFagomraade: String,
    val fagsystemId: String,
)
