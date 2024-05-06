package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Skyldner(
    val linjeId: Int,
    val skyldnerId: String,
    val datoFom: String,
    val tidspktReg: String,
    val brukerid: String,
)
