package no.nav.sokos.oppdrag.attestasjon.domain

import java.time.LocalDate

data class OppdragslinjeWithoutFluff(
    val oppdragsId: Int,
    val linjeId: Int,
    val kodeKlasse: String,
    val datoVedtakFom: LocalDate,
    val datoVedtakTom: LocalDate?,
    val attestert: Boolean,
    val sats: Double,
    val typeSats: String,
    val delytelseId: Int,
)
