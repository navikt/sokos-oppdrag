package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjonRequest(
    val oppdragsId: Int,
    val linjer: List<AttestasjonLinje> = mutableListOf(),
)

@Serializable
data class AttestasjonLinje(
    val linjeId: Int,
    val datoUgyldigFom: String? = "",
)
