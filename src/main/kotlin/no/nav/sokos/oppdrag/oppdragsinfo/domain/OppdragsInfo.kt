package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsInfo(
    val gjelderId: String? = null,
    val oppdragsListe: List<Oppdrag>? = null,
)
