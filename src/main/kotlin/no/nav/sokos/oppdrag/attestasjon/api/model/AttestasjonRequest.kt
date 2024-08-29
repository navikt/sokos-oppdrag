package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjonRequest(
    val gjelderId: String,
    val fagOmraade: String,
    val oppdragsId: Int,
    val brukerId: String,
    val kjorIdag: Boolean = true,
    val linjer: List<AttestasjonLinje> = mutableListOf(),
)

@Serializable
data class AttestasjonLinje(
    val linjeId: Int,
    val attestantId: String,
    val datoUgyldigFom: String? = "",
)
