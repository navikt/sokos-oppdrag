package no.nav.sokos.oppdrag

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import no.nav.sokos.oppdrag.common.Metrics
import no.nav.sokos.oppdrag.common.config.DatabaseConfig
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.commonConfig
import no.nav.sokos.oppdrag.common.config.configureSecurity
import no.nav.sokos.oppdrag.common.config.routingConfig
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

fun main() {
    val applicationState = ApplicationState()
    val applicationConfiguration = PropertiesConfig.Configuration()

    HttpServer(applicationState, applicationConfiguration).start()
}

private class HttpServer(
    private val applicationState: ApplicationState,
    private val applicationConfiguration: PropertiesConfig.Configuration,
    private val databaseConfig: DatabaseConfig = DatabaseConfig(),
    port: Int = 8080,
) {
    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                databaseConfig.close()
                this.stop()
            },
        )
    }

    private val embeddedServer =
        embeddedServer(Netty, port, module = {
            applicationModule(applicationState, applicationConfiguration)
        })

    fun start() {
        applicationState.running = true
        embeddedServer.start(wait = true)
    }

    private fun stop() {
        applicationState.running = false
        embeddedServer.stop(5, 5, TimeUnit.SECONDS)
    }
}

class ApplicationState(
    alive: Boolean = true,
    ready: Boolean = false,
) {
    var initialized: Boolean by Delegates.observable(alive) { _, _, newValue ->
        if (!newValue) Metrics.appStateReadyFalse.inc()
    }
    var running: Boolean by Delegates.observable(ready) { _, _, newValue ->
        if (!newValue) Metrics.appStateRunningFalse.inc()
    }
}

fun Application.applicationModule(
    applicationState: ApplicationState,
    applicationConfiguration: PropertiesConfig.Configuration,
) {
    commonConfig()
    configureSecurity(applicationConfiguration.azureAdConfig, applicationConfiguration.useAuthentication)
    routingConfig(applicationState, applicationConfiguration.useAuthentication)
}
