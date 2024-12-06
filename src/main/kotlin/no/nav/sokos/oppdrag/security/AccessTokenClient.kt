package no.nav.sokos.oppdrag.security

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient

private val logger = KotlinLogging.logger {}
private const val IDENTITY_PROVIDER = "azuread"

class AccessTokenClient(
    private val azureAdProperties: PropertiesConfig.AzureAdProperties = PropertiesConfig.AzureAdProperties(),
    private val azureAdScope: String,
    private val client: HttpClient = createHttpClient(),
    // private val azureAdAccessTokenUrl: String = "https://login.microsoftonline.com/${azureAdProperties.tenantId}/oauth2/v2.0/token",
) {
    suspend fun getSystemToken(): String {
        logger.info { "Henter accesstoken" }
        val accessToken = getAccessToken()
        return accessToken.token
    }

    private suspend fun getAccessToken(): AccessToken {
        val response: HttpResponse =
            client.post(azureAdProperties.naisTokenEndpoint) {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("identity_provider", IDENTITY_PROVIDER)
                            append("target", azureAdScope)
                        },
                    ),
                )
            }

        return when {
            response.status.isSuccess() -> response.body()
            else -> {
                val errorMessage =
                    "GetAccessToken returnerte ${response.status} med feilmelding: ${response.errorMessage()}"
                logger.error { errorMessage }
                throw RuntimeException(errorMessage)
            }
        }
    }
}

suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["error_description"]?.jsonPrimitive?.content

@Serializable
private data class AccessToken(
    @SerialName("access_token")
    val token: String,
)
