package no.nav.sokos.oppdrag.integration.skjerming

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
class SkjermetClientImpl(
    private val httpClient: HttpClient = createHttpClient(),
    private val skjermetUrl: String = PropertiesConfig.EksterneHostProperties().skjermetUrl,
    private val skjermetScope: String = PropertiesConfig.EksterneHostProperties().skjermetScope,
    private val accessTokenProvider: AccessTokenClient = AccessTokenClient(azureAdScope = skjermetScope),
) : SkjermetClient {
    override suspend fun erPersonSkjermet(personIdent: String): Boolean {
        logger.info("Henter accesstoken mot skjerming-tjenesten")
        val token = accessTokenProvider.getSystemToken()

        val skjermetUrl = "$skjermetUrl/skjermet"
        logger.info("Kaller skjermingtjeneste med url: $skjermetUrl")

        val response =
            httpClient.post(skjermetUrl) {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(SkjermetPersonRequest(personIdent))
                header("Authorization", "Bearer $token")
            }

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Kall mot Skjerming-tjeneste feilet: status = $response.status")
        }

        return response.bodyAsText() == "true"
    }
}

data class SkjermetPersonRequest(val personident: String)
