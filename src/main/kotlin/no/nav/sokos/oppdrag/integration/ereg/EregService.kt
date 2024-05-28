package no.nav.sokos.oppdrag.integration.ereg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.Metrics
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.httpClient
import no.nav.sokos.oppdrag.oppdragsinfo.config.ApiError
import no.nav.sokos.oppdrag.oppdragsinfo.util.EregException
import org.slf4j.MDC
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

class EregService(
    private val eregUrl: String = PropertiesConfig.EksterneHostConfig().eregUrl,
    private val client: HttpClient = httpClient,
) {
    suspend fun getOrganisasjonsNavn(organisasjonsNummer: String): Organisasjon {
        logger.info("Henter organisasjonsnavn for $organisasjonsNummer fra Ereg.")
        val response =
            client.get("$eregUrl/v2/organisasjon/$organisasjonsNummer/noekkelinfo") {
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
            }
        Metrics.eregCallCounter.labelValues("${response.status.value}").inc()
        return when {
            response.status.isSuccess() -> response.body<Organisasjon>()
            response.status.value == 400 -> {
                throw EregException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        HttpStatusCode.BadRequest.description,
                        response.errorMessage() ?: "",
                        "$eregUrl/v2/organisasjon/{orgnummer}/noekkelinfo",
                    ),
                    response,
                )
            }

            response.status.value == 404 -> {
                throw EregException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        HttpStatusCode.NotFound.description,
                        response.errorMessage() ?: "",
                        "$eregUrl/v2/organisasjon/{orgnummer}/noekkelinfo",
                    ),
                    response,
                )
            }

            else -> {
                throw EregException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        response.status.description,
                        response.errorMessage() ?: "",
                        "$eregUrl/v2/organisasjon/{orgnummer}/noekkelinfo",
                    ),
                    response,
                )
            }
        }
    }
}

suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["melding"]?.jsonPrimitive?.content
