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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import java.time.Instant

private val logger = KotlinLogging.logger {}

class AccessTokenClient(
    private val azureAdProperties: PropertiesConfig.AzureAdProperties = PropertiesConfig.AzureAdProperties(),
    private val azureAdScope: String,
    private val client: HttpClient = createHttpClient(),
    private val azureAdAccessTokenUrl: String = "https://login.microsoftonline.com/${azureAdProperties.tenantId}/oauth2/v2.0/token",
) {
    private val mutex = Mutex()

    @Volatile
    private var token: AccessToken = runBlocking { AccessToken(getAccessToken()) }

    suspend fun getSystemToken(): String {
        val expiresInToMinutes = Instant.now().plusSeconds(120L)
        return mutex.withLock {
            when {
                token.expiresAt.isBefore(expiresInToMinutes) -> {
                    logger.info { "Henter ny accesstoken" }
                    token = AccessToken(getAccessToken())
                    token.accessToken
                }

                else -> token.accessToken.also { logger.info { "Henter accesstoken fra cache" } }
            }
        }
    }

    private suspend fun getAccessToken(): AzureAccessToken {
        val response: HttpResponse =
            client.post(azureAdAccessTokenUrl) {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("tenant", azureAdProperties.tenantId)
                            append("client_id", azureAdProperties.clientId)
                            append("scope", azureAdScope)
                            append("client_secret", azureAdProperties.clientSecret)
                            append("grant_type", "client_credentials")
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
private data class AzureAccessToken(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

private data class AccessToken(
    val accessToken: String,
    val expiresAt: Instant,
) {
    constructor(azureAccessToken: AzureAccessToken) : this(
        accessToken = azureAccessToken.accessToken,
        expiresAt = Instant.now().plusSeconds(azureAccessToken.expiresIn),
    )
}
