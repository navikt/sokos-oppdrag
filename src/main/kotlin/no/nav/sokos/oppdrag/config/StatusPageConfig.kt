package no.nav.sokos.oppdrag.config

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond

import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSException
import no.nav.sokos.oppdrag.common.exception.ForbiddenException
import no.nav.sokos.oppdrag.integration.exception.IntegrationException

fun StatusPagesConfig.statusPageConfig() {
    exception<Throwable> { call, cause ->
        val (responseStatus, apiError) =
            when (cause) {
                is RequestValidationException -> createApiError(HttpStatusCode.BadRequest, cause.reasons.joinToString(), call)
                is ForbiddenException -> createApiError(HttpStatusCode.InternalServerError, cause.message, call)
                is IntegrationException -> createApiError(HttpStatusCode.BadRequest, cause.message, call)
                is ZOSException -> Pair(HttpStatusCode.allStatusCodes.find { it.value == cause.apiError.status }!!, cause.apiError)
                is IllegalArgumentException -> createApiError(HttpStatusCode.BadRequest, cause.message, call)
                else -> createApiError(HttpStatusCode.InternalServerError, cause.message ?: "En teknisk feil har oppstått. Ta kontakt med utviklerne", call)
            }

        call.application.log.error(
            "Feilet håndtering av ${call.request.httpMethod} - ${call.request.path()} - Status=$responseStatus - Message=${cause.message}",
            cause,
        )
        call.respond(responseStatus, apiError)
    }
}

private fun createApiError(
    status: HttpStatusCode,
    message: String?,
    call: ApplicationCall,
): Pair<HttpStatusCode, ApiError> =
    Pair(
        status,
        ApiError(
            Clock.System.now(),
            status.value,
            status.description,
            message,
            call.request.path(),
        ),
    )

@Serializable
data class ApiError(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
)
