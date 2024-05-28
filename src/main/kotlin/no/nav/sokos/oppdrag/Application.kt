package no.nav.sokos.oppdrag

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import no.nav.sokos.oppdrag.common.config.DatabaseConfig
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.commonConfig
import no.nav.sokos.oppdrag.common.config.configureLifecycleConfig
import no.nav.sokos.oppdrag.common.config.configureSecurity
import no.nav.sokos.oppdrag.common.config.routingConfig
import java.util.concurrent.TimeUnit

fun main() {
    HttpServer().start()
}

private class HttpServer(
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
            applicationModule()
        })

    fun start() {
        embeddedServer.start(wait = true)
    }

    private fun stop() {
        embeddedServer.stop(5, 5, TimeUnit.SECONDS)
    }
}

class ApplicationState(
    var ready: Boolean = true,
    var alive: Boolean = true,
)

fun Application.applicationModule() {
    val applicationState = ApplicationState()
    val applicationConfiguration = PropertiesConfig.Configuration()

    configureLifecycleConfig(applicationState)
    commonConfig()
    configureSecurity(applicationConfiguration.azureAdConfig, applicationConfiguration.useAuthentication)
    routingConfig(applicationState, applicationConfiguration.useAuthentication)
}
