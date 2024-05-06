package no.nav.sokos.oppdrag.oppdragsinfo.integration

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.httpClient
import no.nav.sokos.oppdrag.oppdragsinfo.config.ApiError
import no.nav.sokos.oppdrag.oppdragsinfo.integration.model.TssResponse
import no.nav.sokos.oppdrag.oppdragsinfo.metrics.Metrics
import no.nav.sokos.oppdrag.oppdragsinfo.util.TpException
import no.nav.sokos.oppdrag.oppdragsinfo.util.retry
import org.slf4j.MDC
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

class TpService(
    private val tpHost: String = PropertiesConfig.EksterneHost().tpHost,
    private val client: HttpClient = httpClient,
) {
    suspend fun getLeverandorNavn(tssId: String): TssResponse =
        retry {
            logger.info("Henter leverandørnavn for $tssId fra TP.")
            val response =
                client.get("$tpHost/api/ordninger/tss/$tssId") {
                    header("Nav-Call-Id", MDC.get("x-correlation-id"))
                }
            Metrics.tpCallCounter.labels("${response.status.value}").inc()
            when {
                response.status.isSuccess() -> TssResponse(response.body<String>())

                response.status.value == 404 -> {
                    throw TpException(
                        ApiError(
                            ZonedDateTime.now(),
                            response.status.value,
                            HttpStatusCode.NotFound.description,
                            "Fant ingen leverandørnavn med tssId $tssId",
                            "$tpHost/api/ordninger/tss/{tssId}",
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
                            "$tpHost/api/ordninger/tss/{tssId}",
                        ),
                        response,
                    )
                }
            }
        }
}
