package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Kjoreplan(
    val kodeFaggruppe: String,
    val datoKjores: String,
    val status: String,
    val datoForfall: String,
    val datoOverfores: String? = null,
    val datoBeregnFom: String? = null,
    val datoBeregnTom: String? = null,
)
