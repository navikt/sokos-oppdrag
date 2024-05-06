package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Kravhaver(
    val linjeId: Int,
    val kravhaverId: String,
    val datoFom: String,
    val tidspktReg: String,
    val brukerid: String,
)
