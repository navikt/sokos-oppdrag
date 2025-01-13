package no.nav.sokos.oppdrag.attestasjon.api.model

import kotlinx.serialization.Serializable

import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.ALLE

@Serializable
data class OppdragsRequest(
    val gjelderId: String? = null,
    val fagSystemId: String? = null,
    val kodeFagGruppe: String? = null,
    val kodeFagOmraade: String? = null,
    val attestertStatus: AttestertStatus = ALLE,
)
