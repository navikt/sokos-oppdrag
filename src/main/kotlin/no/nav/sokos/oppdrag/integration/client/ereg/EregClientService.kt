package no.nav.sokos.oppdrag.integration.client.ereg

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import mu.KotlinLogging
import org.slf4j.MDC

import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.integration.exception.IntegrationException
import no.nav.sokos.oppdrag.integration.metrics.Metrics

private val logger = KotlinLogging.logger {}

class EregClientService(
    private val eregUrl: String = PropertiesConfig.EksterneHostProperties().eregUrl,
    private val client: HttpClient = createHttpClient(),
) {
    @OptIn(ExperimentalTime::class)
    suspend fun getOrganisasjonsNavn(organisasjonsNummer: String): Organisasjon {
        logger.info { "Henter organisasjonsnavn for $organisasjonsNummer fra Ereg." }
        val response =
            client.get("$eregUrl/v2/organisasjon/$organisasjonsNummer/noekkelinfo") {
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
            }
        Metrics.eregCallCounter.labelValues("${response.status.value}").inc()

        return when {
            response.status.isSuccess() -> response.body<Organisasjon>()
            else -> {
                throw IntegrationException(
                    ApiError(
                        Clock.System.now(),
                        response.status.value,
                        response.status.description,
                        response.errorMessage() ?: "Noe gikk galt ved oppslag mot Ereg-tjenesten",
                        "$eregUrl/v2/organisasjon/$organisasjonsNummer/noekkelinfo",
                    ),
                    response,
                )
            }
        }
    }
}

private suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["melding"]?.jsonPrimitive?.content

@Serializable
data class Organisasjon(
    @SerialName("navn")
    val navn: Navn,
)

@Serializable
data class Navn(
    @SerialName("sammensattnavn")
    val sammensattnavn: String,
)
