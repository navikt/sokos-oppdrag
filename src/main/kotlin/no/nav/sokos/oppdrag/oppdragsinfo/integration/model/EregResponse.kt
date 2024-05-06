package no.nav.sokos.oppdrag.oppdragsinfo.integration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Organisasjon(
    @SerialName("navn")
    val navn: Navn,
)

@Serializable
data class Navn(
    @SerialName("sammensattnavn")
    val sammensattnavn: String,
)
