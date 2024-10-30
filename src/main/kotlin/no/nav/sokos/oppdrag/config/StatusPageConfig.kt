package no.nav.sokos.oppdrag.config

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import no.nav.sokos.oppdrag.attestasjon.exception.AttestasjonException
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSException
import no.nav.sokos.oppdrag.common.util.ZonedDateTimeSerializer
import no.nav.sokos.oppdrag.integration.client.ereg.EregException
import no.nav.sokos.oppdrag.integration.client.skjerming.SkjermetException
import no.nav.sokos.oppdrag.integration.client.tp.TpException
import java.time.ZonedDateTime

fun StatusPagesConfig.statusPageConfig() {
    exception<Throwable> { call, cause ->
        val (responseStatus, apiError) =
            when (cause) {
                is RequestValidationException -> {
                    Pair(
                        HttpStatusCode.BadRequest,
                        ApiError(
                            ZonedDateTime.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            cause.reasons.joinToString(),
                            call.request.path(),
                        ),
                    )
                }

                is AttestasjonException -> {
                    Pair(
                        HttpStatusCode.BadRequest,
                        ApiError(
                            ZonedDateTime.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            cause.message,
                            call.request.path(),
                        ),
                    )
                }

                is EregException -> {
                    Pair(
                        cause.response.status,
                        cause.apiError,
                    )
                }

                is TpException -> {
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

                is SkjermetException -> {
                    Pair(
                        cause.response.status,
                        cause.apiError,
                    )
                }

                is ClientRequestException -> {
                    Pair(
                        cause.response.status,
                        ApiError(
                            ZonedDateTime.now(),
                            cause.response.status.value,
                            cause.response.status.description,
                            cause.message,
                            call.request.path(),
                        ),
                    )
                }

                else ->
                    Pair(
                        HttpStatusCode.InternalServerError,
                        ApiError(
                            ZonedDateTime.now(),
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
    @Serializable(with = ZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)
