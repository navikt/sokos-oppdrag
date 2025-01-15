package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Fagomraade(
    val antallAttestanter: Int,
    val anviser: String,
    val bilagstypeFinnes: Boolean,
    val klassekodeFinnes: Boolean,
    val kode: String,
    val kodeFaggruppe: String,
    val korraarsakFinnes: Boolean,
    val kodeMotregningsgruppe: String,
    val maksAktiveOppdrag: Int,
    val navn: String,
    val regelFinnes: Boolean,
    val sjekkMotTps: String,
    val sjekkOffnrID: String,
    val tpsDistribusjon: String,
)
