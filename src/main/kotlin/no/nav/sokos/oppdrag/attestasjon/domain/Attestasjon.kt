package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.common.util.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Attestasjon(
    val attestant: String,
    @Serializable(with = LocalDateSerializer::class)
    val datoUgyldigFom: LocalDate,
)
