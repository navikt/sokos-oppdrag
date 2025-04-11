package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Valuta(
    val linjeId: Int,
    val typeValuta: String,
    val datoFom: String,
    val nokkelId: Int,
    val valuta: String,
    val feilreg: String? = null,
    val tidspktReg: String,
    val brukerid: String,
)
