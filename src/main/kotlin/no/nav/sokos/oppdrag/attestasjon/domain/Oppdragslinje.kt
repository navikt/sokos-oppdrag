package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Oppdragslinje(
    val attestert: Boolean,
    val datoVedtakFom: LocalDate,
    val datoVedtakTom: LocalDate? = null,
    val delytelseId: String,
    val kodeKlasse: String,
    val linjeId: Int,
    val oppdragsId: Int,
    val sats: Double,
    val typeSats: String,
    val hovedkontonr: String? = null,
    val underkontonr: String? = null,
    val grad: Int? = null,
    val kid: String? = null,
    val refunderesId: String? = null,
    val skyldnerId: String? = null,
    val utbetalesTilId: String? = null,
)
