package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class TrekkregelKjoreplan(
    val kodeOppgjorstype: String,
    val datoKjores: String,
    val status: String,
    val datoPeriodeFom: String,
    val datoPeriodeTom: String,
)
