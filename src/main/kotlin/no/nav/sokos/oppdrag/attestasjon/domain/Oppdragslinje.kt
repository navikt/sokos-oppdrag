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
    val kontonummer: String,
    val kid: String,
    val skyldner: String,
    val refusjonsid: String,
    val utbetalesTil: String,
    val grad: Int,
)
