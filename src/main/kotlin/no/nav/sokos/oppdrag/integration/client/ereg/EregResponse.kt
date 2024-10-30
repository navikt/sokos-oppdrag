package no.nav.sokos.oppdrag.integration.client.ereg

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
