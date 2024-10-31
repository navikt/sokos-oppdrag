package no.nav.sokos.oppdrag.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSException
import no.nav.sokos.oppdrag.integration.exception.IntegrationException
import no.nav.sokos.oppdrag.oppdragsinfo.exception.OppdragsinfoException

fun StatusPagesConfig.statusPageConfig() {
    exception<Throwable> { call, cause ->
        val (responseStatus, apiError) =
            when (cause) {
                is RequestValidationException -> {
                    Pair(
                        HttpStatusCode.BadRequest,
                        ApiError(
                            Clock.System.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            cause.reasons.joinToString(),
                            call.request.path(),
                        ),
                    )
                }

                is AttestasjonException, is OppdragsinfoException -> {
                    Pair(
                        HttpStatusCode.BadRequest,
                        ApiError(
                            Clock.System.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            cause.message,
                            call.request.path(),
                        ),
                    )
                }

                is IntegrationException -> {
                    Pair(
                        cause.response.status,
                        cause.apiError,
                    )
                }

                is ZOSException -> {
                    Pair(
                        HttpStatusCode.allStatusCodes.find { it.value == cause.apiError.status }!!,
                        cause.apiError,
                    )
                }

                else ->
                    Pair(
                        HttpStatusCode.InternalServerError,
                        ApiError(
                            Clock.System.now(),
                            HttpStatusCode.InternalServerError.value,
                            HttpStatusCode.InternalServerError.description,
                            cause.message ?: "En teknisk feil har oppstått. Ta kontakt med utviklerne",
                            call.request.path(),
                        ),
                    )
            }

        call.application.log.error(
            "Feilet håndtering av ${call.request.httpMethod} - ${call.request.path()} - Status=$responseStatus - Message=${cause.message}",
            cause,
        )
        call.respond(responseStatus, apiError)
    }
}

@Serializable
data class ApiError(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
)
