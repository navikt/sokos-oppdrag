package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjonsRequest(
    val gjelderId: String,
    val fagOmraade: String,
    val oppdragsId: Int,
    val brukerId: String,
    val kjorIdag: Boolean = true,
    val linjer: List<AttestasjonsLinje> = mutableListOf<AttestasjonsLinje>(),
)

@Serializable
data class AttestasjonsLinje(
    val linjeId: Int,
    val attestantId: String,
    val datoUgyldigFom: String? = "",
)
