package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class LinjeEnhet(
    val linjeId: Int,
    val typeEnhet: String,
    val enhet: String? = null,
    val datoFom: String,
    val nokkelId: Int,
    val tidspktReg: String,
    val brukerid: String,
)
