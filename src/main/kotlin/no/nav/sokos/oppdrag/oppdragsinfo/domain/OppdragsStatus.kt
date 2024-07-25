package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsStatus(
    val kodeStatus: String,
    val tidspktReg: String,
    val brukerid: String,
)
