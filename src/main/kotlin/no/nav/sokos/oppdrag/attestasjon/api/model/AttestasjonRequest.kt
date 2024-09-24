package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjonRequest(
    val gjelderId: String,
    val fagsystemId: String,
    val kodeFagomraade: String,
    val oppdragsId: Int,
    val linjer: List<AttestasjonLinje> = mutableListOf(),
)

@Serializable
data class AttestasjonLinje(
    val linjeId: Int,
    val attestantIdent: String? = null,
    val datoUgyldigFom: String? = "",
)
