package no.nav.sokos.oppdrag.oppdragsinfo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ompostering(
    val id: String,
    val kodeFaggruppe: String,
    val lopenr: Int,
    val ompostering: String,
    val omposteringFom: String? = null,
    val feilReg: String,
    val beregningsId: Int? = null,
    val utfort: String,
    val brukerid: String,
    val tidspktReg: String,
)
