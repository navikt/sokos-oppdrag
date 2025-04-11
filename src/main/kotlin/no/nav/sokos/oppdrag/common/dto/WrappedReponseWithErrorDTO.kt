package no.nav.sokos.oppdrag.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class WrappedReponseWithErrorDTO<T>(
    var data: List<T> = emptyList(),
    var errorMessage: String? = null,
)
