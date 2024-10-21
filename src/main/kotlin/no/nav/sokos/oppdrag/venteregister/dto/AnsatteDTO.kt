package no.nav.sokos.oppdrag.venteregister.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnsatteDTO(
    val id: Int,
    val name: String,
    val profession: String,
)
