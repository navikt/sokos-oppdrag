package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Attestasjon(
    val attestantId: String,
    val datoUgyldigFom: LocalDate,
)
