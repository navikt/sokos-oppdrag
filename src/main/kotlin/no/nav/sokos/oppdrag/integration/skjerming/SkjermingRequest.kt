package no.nav.sokos.oppdrag.integration.skjerming

import kotlinx.serialization.Serializable

@Serializable
data class SkjermingRequest(val personidenter: List<String>)
