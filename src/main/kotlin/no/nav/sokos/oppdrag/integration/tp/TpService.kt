package no.nav.sokos.oppdrag.integration.tp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.Metrics
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.httpClient
import no.nav.sokos.oppdrag.oppdragsinfo.config.ApiError
import no.nav.sokos.oppdrag.oppdragsinfo.util.TpException
import org.slf4j.MDC
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

class TpService(
    private val tpUrl: String = PropertiesConfig.EksterneHostProperties().tpUrl,
    private val client: HttpClient = httpClient,
) {
    suspend fun getLeverandorNavn(tssId: String): TssResponse {
        logger.info("Henter leverandørnavn for $tssId fra TP.")
        val response =
            client.get("$tpUrl/api/ordninger/tss/$tssId") {
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
            }
        Metrics.tpCallCounter.labelValues("${response.status.value}").inc()
        return when {
            response.status.isSuccess() -> TssResponse(response.body<String>())

            response.status.value == 404 -> {
                throw TpException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        HttpStatusCode.NotFound.description,
                        "Fant ingen leverandørnavn med tssId $tssId",
                        "$tpUrl/api/ordninger/tss/{tssId}",
                    ),
                    response,
                )
            }

            else -> {
                throw TpException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        response.status.description,
                        "Noe gikk galt ved oppslag av $tssId i TP",
                        "$tpUrl/api/ordninger/tss/{tssId}",
                    ),
                    response,
                )
            }
        }
    }
}
