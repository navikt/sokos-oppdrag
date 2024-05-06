package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Maksdato(
    val linjeId: Int,
    val maksdato: String,
    val datoFom: String,
    val tidspktReg: String,
    val brukerid: String,
)
