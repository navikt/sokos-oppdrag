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
    val fagGruppe: String,
    val fagOmraade: String,
    val fagSystemId: String,
    val gjelderId: String,
    val kodeFagOmraade: String,
    val kodeKlasse: String,
    val kostnadsStedForOppdrag: String,
    val kostnadsStedForOppdragsLinje: String? = null,
    val linjeId: String,
    val oppdragsId: String,
    val sats: Double,
    val satstype: String,
)
