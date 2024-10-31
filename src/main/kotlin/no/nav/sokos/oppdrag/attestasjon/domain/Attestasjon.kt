package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Attestasjon(
    val attestant: String,
    val datoUgyldigFom: LocalDate,
)
