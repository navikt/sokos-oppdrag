package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.common.util.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Oppdragslinje(
    val attestert: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    val datoVedtakFom: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val datoVedtakTom: LocalDate? = null,
    val delytelseId: String,
    val kodeKlasse: String,
    val linjeId: Int,
    val oppdragsId: Int,
    val sats: Double,
    val typeSats: String,
)
