package no.nav.sokos.oppdrag.integration.skjerming

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.config.errorMessage
import no.nav.sokos.oppdrag.integration.metrics.Metrics
import no.nav.sokos.oppdrag.security.AccessTokenClient
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

/**
 * Oppslag mot PIP-tjenesten til skjermingsløsningen for å sjekke om en person er skjermet.
 *
 * Skjermede personer er NAV-ansatte med familiemedlemmer.
 *
 * https://skjermede-personer-pip.intern.nav.no/swagger-ui/index.html#/skjerming-pip/isSkjermetPost
 *
 */
class SkjermetClientService(
    private val httpClient: HttpClient = createHttpClient(),
    private val skjermetUrl: String = PropertiesConfig.EksterneHostProperties().skjermetUrl,
    private val skjermetScope: String = PropertiesConfig.EksterneHostProperties().skjermetScope,
    private val accessTokenClient: AccessTokenClient = AccessTokenClient(azureAdScope = skjermetScope),
) {
    suspend fun isSkjermedePersonerInSkjermingslosningen(personIdenter: List<String>): Map<String, Boolean> {
        logger.info("Henter accesstoken mot skjerming-tjenesten")
        val token = accessTokenClient.getSystemToken()

        val skjermetUrl = "$skjermetUrl/skjermetBulk"

        val response =
            httpClient.post(skjermetUrl) {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(SkjermingRequest(personIdenter))
                header("Authorization", "Bearer $token")
            }

        Metrics.nomCallCounter.labelValues("${response.status.value}").inc()

        return when {
            response.status.isSuccess() -> response.body<Map<String, Boolean>>()
            else -> {
                throw SkjermetException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        response.status.description,
                        response.errorMessage() ?: "Noe gikk galt ved oppslag mot $skjermetUrl",
                        skjermetUrl,
                    ),
                    response,
                )
            }
        }
    }
}

data class SkjermetException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)