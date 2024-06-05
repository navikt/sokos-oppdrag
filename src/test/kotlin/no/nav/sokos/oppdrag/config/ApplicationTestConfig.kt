package no.nav.sokos.oppdrag.config

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback

const val APPLICATION_JSON = "application/json"
const val BASE_API_PATH = "/api/v1"
const val OPPDRAGSINFO_API_PATH = "/oppdragsinfo"

fun ApplicationTestBuilder.configureTestApplication() {
    val mapApplicationConfig = MapApplicationConfig()
    environment {
        config = mapApplicationConfig
    }

    application {
        commonConfig()
    }
}

fun MockOAuth2Server.mockAuthConfig() =
    PropertiesConfig.AzureAdProperties(
        wellKnownUrl = wellKnownUrl("default").toString(),
        clientId = "default",
    )

fun MockOAuth2Server.token(): String =
    issueToken(
        issuerId = "default",
        clientId = "default",
        tokenCallback =
            DefaultOAuth2TokenCallback(
                claims =
                    mapOf(
                        "NAVident" to "Z123456",
                    ),
            ),
    ).serialize()
