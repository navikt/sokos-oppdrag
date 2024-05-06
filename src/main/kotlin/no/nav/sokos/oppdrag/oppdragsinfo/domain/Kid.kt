package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Kid(
    val linjeId: Int,
    val kid: String,
    val datoFom: String,
    val tidspktReg: String,
    val brukerid: String,
)
