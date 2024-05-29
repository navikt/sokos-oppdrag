package no.nav.sokos.oppdrag.config

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.sokos.oppdrag.common.config.ApplicationState
import no.nav.sokos.oppdrag.common.config.commonConfig
import no.nav.sokos.oppdrag.common.naisApi

const val APPLICATION_JSON = "application/json"
const val BASE_API_PATH = "/api/v1"
const val OPPDRAGSINFO_API_PATH = "/oppdragsinfo"

fun ApplicationTestBuilder.configureTestApplication() {
    val mapApplicationConfig = MapApplicationConfig()
    environment {
        config = mapApplicationConfig
    }

    application {
        val applicationState = ApplicationState()

        commonConfig()
        routing {
            naisApi(applicationState)
        }
    }
}
