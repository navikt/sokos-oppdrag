package no.nav.sokos.oppdrag.config

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

private val logger = KotlinLogging.logger {}

fun createHttpClient(setProxy: Boolean = true): HttpClient {
    return HttpClient(Apache) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                },
            )
        }
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(5)
            modifyRequest { request ->
                logger.warn { "$retryCount retry feilet mot: ${request.url}" }
            }
            exponentialDelay()
        }

        if (setProxy) {
            engine {
                customizeClient {
                    setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                }
            }
        }
    }
}

suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["errorMessage"]?.jsonPrimitive?.content

suspend fun HttpResponse.errorDetails() = body<JsonElement>().jsonObject["errorDetails"]?.jsonPrimitive?.content
