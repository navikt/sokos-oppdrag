package no.nav.sokos.oppdrag.integration.client.tp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.integration.metrics.Metrics
import org.slf4j.MDC

private val logger = KotlinLogging.logger {}

class TpClientService(
    private val tpUrl: String = PropertiesConfig.EksterneHostProperties().tpUrl,
    private val client: HttpClient = createHttpClient(),
) {
    suspend fun getLeverandorNavn(tssId: String): String {
        logger.info { "Henter leverandørnavn for $tssId fra TP." }
        val response =
            client.get("$tpUrl/api/ordninger/tss/$tssId") {
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
            }
        Metrics.tpCallCounter.labelValues("${response.status.value}").inc()
        return when {
            response.status.isSuccess() -> response.body<String>()

            response.status.value == 404 -> {
                throw TpException(
                    ApiError(
                        Clock.System.now(),
                        response.status.value,
                        HttpStatusCode.NotFound.description,
                        "Fant ingen leverandørnavn med tssId $tssId",
                        "$tpUrl/api/ordninger/tss/$tssId",
                    ),
                    response,
                )
            }

            else -> {
                throw TpException(
                    ApiError(
                        Clock.System.now(),
                        response.status.value,
                        response.status.description,
                        "Noe gikk galt ved oppslag mot TP-tjenesten",
                        "$tpUrl/api/ordninger/tss/$tssId",
                    ),
                    response,
                )
            }
        }
    }
}

data class TpException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)
