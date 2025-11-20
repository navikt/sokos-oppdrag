package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Bilagstype(
    val kodeFagomraade: String,
    val typeBilag: String,
    val datoFom: String,
    val datoTom: String?,
)
