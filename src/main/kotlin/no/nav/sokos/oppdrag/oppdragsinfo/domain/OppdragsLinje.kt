package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsLinje(
    val linjeId: Int,
    val kodeKlasse: String,
    val datoVedtakFom: String,
    val datoVedtakTom: String? = null,
    val sats: Double,
    val typeSats: String,
    val kodeStatus: String,
    val datoFom: String,
    val linjeIdKorr: Int? = null,
    val attestert: String?,
    val delytelseId: String,
    val utbetalesTilId: String,
    val refunderesId: String? = null,
    val brukerId: String,
    val tidspktReg: String,
    val vedtakssats: Double?,
    val hovedkontonr: String?,
    val underkontonr: String?,
)
