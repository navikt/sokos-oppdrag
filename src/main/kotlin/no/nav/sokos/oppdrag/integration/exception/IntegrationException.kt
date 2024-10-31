package no.nav.sokos.oppdrag.integration.exception

import io.ktor.client.statement.HttpResponse
import no.nav.sokos.oppdrag.config.ApiError

data class IntegrationException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)
