package no.nav.sokos.oppdrag.integration.skjerming

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.security.AccessTokenClient

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
        logger.info("Kaller skjermingtjeneste med url: $skjermetUrl")

        val response =
            httpClient.post(skjermetUrl) {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(SkjermetPersonerRequest(personIdenter))
                header("Authorization", "Bearer $token")
            }

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Kall mot Skjerming-tjeneste feilet: status = $response.status")
        }

        return response.body<Map<String, Boolean>>()
    }
}

@Serializable
data class SkjermetPersonerRequest(val personidenter: List<String>)
