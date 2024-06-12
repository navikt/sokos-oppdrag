package no.nav.sokos.oppdrag.common.model

import kotlinx.serialization.Serializable

@Serializable
data class NokkelinfoOmOppdrag(
    val fagsystemId: String,
    val oppdragsId: Int,
    val navnFagGruppe: String,
    val navnFagOmraade: String,
    val kjorIdag: String,
    val typeBilag: String? = null,
    val kodeStatus: String,
)
