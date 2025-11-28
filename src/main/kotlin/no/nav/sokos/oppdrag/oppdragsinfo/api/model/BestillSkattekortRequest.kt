package no.nav.sokos.oppdrag.oppdragsinfo.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BestillSkattekortRequest(
    val gjelderId: String,
    val inntektsAar: Int? = null,
)
