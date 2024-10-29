package no.nav.sokos.oppdrag.integration.pdl

import com.expediagroup.graphql.client.types.GraphQLClientError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import mu.KotlinLogging
import no.nav.pdl.HentPersonBolk
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.integration.metrics.Metrics
import no.nav.sokos.oppdrag.security.AccessTokenClient
import org.slf4j.MDC

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class PdlClientService(
    private val pdlUrl: String = PropertiesConfig.EksterneHostProperties().pdlUrl,
    private val pdlScope: String = PropertiesConfig.EksterneHostProperties().pdlScope,
    private val client: HttpClient = createHttpClient(),
    private val accessTokenClient: AccessTokenClient = AccessTokenClient(azureAdScope = pdlScope),
) {
    suspend fun getPerson(identer: List<String>): Map<String, Person> {
        val request = HentPersonBolk(HentPersonBolk.Variables(identer = identer))

        logger.info { "Henter accesstoken for oppslag mot PDL" }
        val accessToken = accessTokenClient.getSystemToken()

        logger.info { "Henter Person fra PDL" }
        val response =
            client.post("$pdlUrl/graphql") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("behandlingsnummer", "B154")
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        Metrics.pdlCallCounter.labelValues("${response.status.value}").inc()

        return when {
            response.status.isSuccess() -> {
                val result = response.body<GraphQLResponse<HentPersonBolk.Result>>()
                if (result.errors?.isNotEmpty() == true) {
                    handleErrors(result.errors, result.data?.hentPersonBolk?.map { it.ident } ?: emptyList())
                }
                result.data?.hentPersonBolk
                    ?.filter { item -> item.person != null }
                    ?.map { item -> item.ident to item.person!! }
                    ?.toMap() ?: emptyMap()
            }
            else -> {
                secureLogger.error { "Noe gikk galt ved oppslag mot PDL for ident: $identer" }
                throw ClientRequestException(
                    response,
                    "Noe gikk galt ved oppslag mot PDL",
                )
            }
        }
    }

    private fun handleErrors(
        errors: List<GraphQLClientError>,
        ident: List<String>,
    ) {
        val errorExtensions = errors.mapNotNull { it.extensions }
        val path = errors.mapNotNull { it.path?.firstOrNull() }
        val errorCode = errorExtensions.mapNotNull { it["code"] }
        val errorMessage = errors.joinToString { it.message }

        val exceptionMessage = "(Path: $path, Code: $errorCode, Message: $errorMessage)"
        val secureExceptionMessage = "(Identer: ${ident.joinToString()}, Path: $path, Code: $errorCode, Message: $errorMessage)"

        secureLogger.error { secureExceptionMessage }

        throw PdlException(
            exceptionMessage,
        )
    }
}

data class PdlException(
    override val message: String,
) : Exception(message)
