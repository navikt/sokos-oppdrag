package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Faggruppe(
    val kodeFaggruppe: String,
    val navnFaggruppe: String,
    val skatteprosent: Int,
    val ventedager: Int,
    val klassekodeFeil: String?,
    val klassekodeJustering: String?,
    val destinasjon: String?,
    val reskontroOppdrag: String?,
    val klassekodeMotpFeil: String?,
    val klassekodeMotpTrekk: String?,
    val klassekodeMotpInnkr: String?,
    val prioritet: Int,
    val onlineBeregning: Boolean,
    val pensjon: Boolean?,
    val oereavrunding: Boolean,
    val samordnetBeregning: String,
    val antallFagomraader: Int,
)
