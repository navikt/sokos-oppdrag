package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.common.model.NokkelinfoOmOppdrag

@Serializable
data class OppdragsinfoTreffliste(
    val gjelderId: String? = null,
    val oppdragsListe: List<NokkelinfoOmOppdrag>? = null,
)
