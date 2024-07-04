package no.nav.sokos.oppdrag.attestasjon.model

import kotlinx.serialization.Serializable

@Serializable
data class AttestasjondetaljerRequestBody(val oppdragsIder: List<Int>)
