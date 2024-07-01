package no.nav.sokos.oppdrag.integration.pdl

import com.expediagroup.graphql.client.types.GraphQLClientError
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
import no.nav.pdl.HentPerson
import no.nav.pdl.hentperson.Person
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.config.httpClient
import no.nav.sokos.oppdrag.security.AccessTokenClient
import org.slf4j.MDC

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class PdlService(
    private val pdlUrl: String = PropertiesConfig.EksterneHostProperties().pdlUrl,
    private val pdlScope: String = PropertiesConfig.EksterneHostProperties().pdlScope,
    private val accessTokenClient: AccessTokenClient = AccessTokenClient(azureAdScope = pdlScope),
) {
    suspend fun getPersonNavn(ident: String): Person? {
        val request = HentPerson(HentPerson.Variables(ident = ident))

        logger.info { "Henter accesstoken for oppslag mot PDL" }
        val accessToken = accessTokenClient.getSystemToken()

        logger.info { "Henter Person fra PDL" }
        val response =
            httpClient.post("$pdlUrl/graphql") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("behandlingsnummer", "B154")
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        return when {
            response.status.isSuccess() -> {
                val result = response.body<GraphQLResponse<HentPerson.Result>>()
                if (result.errors?.isNotEmpty() == true) {
                    handleErrors(result.errors, ident)
                }
                result.data?.hentPerson
            }

            else -> {
                throw ClientRequestException(
                    response,
                    "Noe gikk galt ved oppslag av person med ident $ident i PDL",
                )
            }
        }
    }

    private fun handleErrors(
        errors: List<GraphQLClientError>,
        ident: String,
    ): Person? {
        val errorExtensions = errors.mapNotNull { it.extensions }
        val path = errors.mapNotNull { it.path?.firstOrNull() }
        val errorCode = errorExtensions.mapNotNull { it["code"] }
        val errorMessage = errors.joinToString { it.message }

        val exceptionMessage = "(Path: $path, Code: $errorCode, Message: $errorMessage)"
        val secureExceptionMessage = "(Ident: $ident, Path: $path, Code: $errorCode, Message: $errorMessage)"

        secureLogger.error { secureExceptionMessage }

        throw PdlException(
            exceptionMessage,
        )
    }
}

data class PdlException(
    override val message: String,
) : Exception(message)
