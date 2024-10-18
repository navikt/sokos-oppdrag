package no.nav.sokos.oppdrag.venteregister.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnsatteDTO(
    val name: String,
    val profession: String,
)
