package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Oppdrag(
    val fagsystemId: String,
    val oppdragsId: Int,
    val navnFaggruppe: String,
    val navnFagomraade: String,
    val kjorIdag: String,
    val typeBilag: String? = null,
    val kodeStatus: String,
    val kostnadssted: String? = null,
    val ansvarssted: String? = null,
)
