package no.nav.sokos.oppdrag.config

import java.net.ProxySelector

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import mu.KotlinLogging
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner

private val logger = KotlinLogging.logger {}

fun createHttpClient(setProxy: Boolean = true): HttpClient =
    HttpClient(Apache5) {
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

suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["errorMessage"]?.jsonPrimitive?.content

suspend fun HttpResponse.errorDetails() = body<JsonElement>().jsonObject["errorDetails"]?.jsonPrimitive?.content
