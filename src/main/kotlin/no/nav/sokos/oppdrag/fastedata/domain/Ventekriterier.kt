package no.nav.sokos.oppdrag.fastedata.domain

data class Ventekriterier(
    val kodeFaggruppe: String,
    val typeBilag: String,
    val datoFom: String,
    val belopBrutto: String,
    val belopNetto: String,
    val antDagerEldreEnn: Int?,
    val tidligereAar: Boolean?,
)
