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
    val grad: Int? = null,
    val kid: String? = null,
    val refusjonsid: String? = null,
    val skyldner: String? = null,
    val utbetalesTil: String? = null,
)
