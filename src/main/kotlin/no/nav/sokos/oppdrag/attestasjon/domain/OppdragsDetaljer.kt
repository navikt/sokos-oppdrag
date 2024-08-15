package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val ansvarsStedForOppdrag: String? = null,
    val ansvarsStedForOppdragsLinje: String? = null,
    val antallAttestanter: Int,
    val attestant: String? = null,
    val datoUgyldigFom: String? = null,
    val datoVedtakFom: String,
    val datoVedtakTom: String? = null,
    val delytelsesId: String,
    val fagSystemId: String,
    val kodeKlasse: String,
    val kostnadsStedForOppdrag: String,
    val kostnadsStedForOppdragsLinje: String? = null,
    val linjeId: String,
    val navnFagGruppe: String,
    val navnFagOmraade: String,
    val oppdragGjelderId: String,
    val oppdragsId: String,
    val sats: Double,
    val satstype: String,
)
