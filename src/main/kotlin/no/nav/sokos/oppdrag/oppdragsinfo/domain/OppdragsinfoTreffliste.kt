package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsinfoTreffliste(
    val gjelderId: String? = null,
    val oppdragsListe: List<Oppdragsegenskaper>? = null,
)
