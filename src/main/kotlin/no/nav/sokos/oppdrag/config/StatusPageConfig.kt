package no.nav.sokos.oppdrag.config

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.path
import io.ktor.server.response.respond
import mu.KotlinLogging

import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSException
import no.nav.sokos.oppdrag.integration.exception.IntegrationException

private val logger = KotlinLogging.logger {}

fun StatusPagesConfig.statusPageConfig() {
    exception<RequestValidationException> { call, cause ->
        call.logOgResponder(HttpStatusCode.BadRequest, cause.reasons.joinToString(), cause)
    }

    exception<BadRequestException> { call, cause ->
        call.logOgResponder(HttpStatusCode.BadRequest, cause.message, cause)
    }

    exception<AttestasjonException> { call, cause ->
        call.logOgResponder(HttpStatusCode.BadRequest, cause.message, cause)
    }

    exception<IntegrationException> { call, cause ->
        call.logOgResponder(HttpStatusCode.BadRequest, cause.message, cause)
    }

    exception<ZOSException> { call, cause ->
        val status = HttpStatusCode.allStatusCodes.find { it.value == cause.apiError.status }!!
        call.logOgResponder(status, cause.apiError, cause)
    }

    exception<Throwable> { call, cause ->
        call.logOgResponder(
            HttpStatusCode.InternalServerError,
            cause.message ?: "En teknisk feil har oppstått. Ta kontakt med utviklerne",
            cause,
        )
    }
}

private suspend fun ApplicationCall.logOgResponder(
    status: HttpStatusCode,
    message: String?,
    cause: Throwable,
) {
    logOgResponder(status, createApiError(status, message, this), cause)
}

private suspend fun ApplicationCall.logOgResponder(
    status: HttpStatusCode,
    apiError: ApiError,
    cause: Throwable,
) {
    if (status.value >= 500) {
        logger.error(marker = TEAM_LOGS_MARKER, cause) { "Feil på ${request.path()}: ${cause.message}" }
    } else {
        logger.warn(marker = TEAM_LOGS_MARKER) { "Feil på ${request.path()}: ${cause.message}" }
    }
    respond(status, apiError)
}

@OptIn(ExperimentalTime::class)
private fun createApiError(
    status: HttpStatusCode,
    message: String?,
    call: ApplicationCall,
): ApiError =
    ApiError(
        Clock.System.now(),
        status.value,
        status.description,
        message,
        call.request.path(),
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
