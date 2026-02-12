package no.nav.sokos.oppdrag.config

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.path
import io.ktor.server.response.respond

import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSException
import no.nav.sokos.oppdrag.integration.exception.IntegrationException

fun StatusPagesConfig.statusPageConfig() {
    exception<Throwable> { call, cause ->
        val (responseStatus, apiError) =
            when (cause) {
                is RequestValidationException -> createApiError(HttpStatusCode.BadRequest, cause.reasons.joinToString(), call)
                is AttestasjonException, is IntegrationException -> createApiError(HttpStatusCode.BadRequest, cause.message, call)
                is ZOSException -> Pair(HttpStatusCode.allStatusCodes.find { it.value == cause.apiError.status }!!, cause.apiError)
                is IllegalArgumentException -> createApiError(HttpStatusCode.BadRequest, cause.message, call)
                else -> createApiError(HttpStatusCode.InternalServerError, cause.message ?: "En teknisk feil har oppstått. Ta kontakt med utviklerne", call)
            }
        call.respond(responseStatus, apiError)
    }
}

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
@Serializable
data class ApiError(
    val timestamp: @Contextual Instant,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
)
