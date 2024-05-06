package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Grad(
    val linjeId: Int,
    val typeGrad: String,
    val grad: Int,
    val tidspktReg: String,
    val brukerid: String,
)
