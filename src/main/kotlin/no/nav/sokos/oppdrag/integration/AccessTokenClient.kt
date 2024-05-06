package no.nav.sokos.oppdrag.integration

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
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.httpClient
import no.nav.sokos.oppdrag.oppdragsinfo.util.retry
import java.time.Instant

private val logger = KotlinLogging.logger {}

class AccessTokenClient(
    private val azureAdClientConfig: PropertiesConfig.AzureAdConfig = PropertiesConfig.AzureAdConfig(),
    private val pdlScope: String = PropertiesConfig.EksterneHostConfig().pdlScope,
    private val client: HttpClient = httpClient,
    private val aadAccessTokenUrl: String = "https://login.microsoftonline.com/${azureAdClientConfig.tenantId}/oauth2/v2.0/token",
) {
    private val mutex = Mutex()

    @Volatile
    private var token: AccessToken = runBlocking { AccessToken(getAccessToken()) }

    suspend fun getSystemToken(): String {
        val expiresInToMinutes = Instant.now().plusSeconds(120L)
        return mutex.withLock {
            when {
                token.expiresAt.isBefore(expiresInToMinutes) -> {
                    logger.info("Henter ny accesstoken")
                    token = AccessToken(getAccessToken())
                    token.accessToken
                }

                else -> token.accessToken.also { logger.info("Henter accesstoken fra cache") }
            }
        }
    }

    private suspend fun getAccessToken(): AzureAccessToken =
        retry {
            val response: HttpResponse =
                client.post(aadAccessTokenUrl) {
                    accept(ContentType.Application.Json)
                    method = HttpMethod.Post
                    setBody(
                        FormDataContent(
                            Parameters.build {
                                append("tenant", azureAdClientConfig.tenantId)
                                append("client_id", azureAdClientConfig.clientId)
                                append("scope", pdlScope)
                                append("client_secret", azureAdClientConfig.clientSecret)
                                append("grant_type", "client_credentials")
                            },
                        ),
                    )
                }

            when {
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
