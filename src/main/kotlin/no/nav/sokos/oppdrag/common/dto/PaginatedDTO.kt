package no.nav.sokos.oppdrag.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedDTO<T>(
    val data: List<T>,
    val page: Int,
    val rows: Int,
    val total: Int,
)

fun <T> List<T>.toPaginatedDTO(
    page: Int,
    rows: Int,
    totalItems: Int,
): PaginatedDTO<T> =
    PaginatedDTO(
        data = this,
        page = page,
        rows = rows,
        total = totalItems,
    )
