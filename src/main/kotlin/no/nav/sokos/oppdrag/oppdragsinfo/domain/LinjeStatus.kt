package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class LinjeStatus(
    val status: String,
    val datoFom: String,
    val tidspktReg: String,
    val brukerid: String,
)
