package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Attestasjonsdata(
    val kodeFaggruppe: String,
    val navnFaggruppe: String,
    val kodeFagomraade: String,
    val navnFagomraade: String,
    val oppdragsId: Int,
    val fagsystemId: String,
    val oppdragGjelderId: String,
    val antAttestanter: Int,
    val linjeId: Int,
    val attestert: String,
    val datoVedtakFom: String,
    val datoVedtakTom: String?,
    val kodeStatus: String,
)
