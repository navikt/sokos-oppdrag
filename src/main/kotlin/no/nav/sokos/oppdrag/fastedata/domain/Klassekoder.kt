package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Klassekoder(
    val kodeKlasse: String,
    val kodeFagomraade: String,
    val artID: Int,
    val datoFom: String,
    val datoTom: String?,
    val hovedkontoNr: String,
    val underkontoNr: String,
)
