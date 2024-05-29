package no.nav.sokos.oppdrag

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import no.nav.sokos.oppdrag.common.config.ApplicationState
import no.nav.sokos.oppdrag.common.config.DatabaseConfig
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.applicationLifeCycleConfig
import no.nav.sokos.oppdrag.common.config.commonConfig
import no.nav.sokos.oppdrag.common.config.routingConfig
import no.nav.sokos.oppdrag.common.config.securityConfig
import java.util.concurrent.TimeUnit

fun main() {
    HttpServer(port = 8080).start()
}

fun Application.applicationModule() {
    val applicationState = ApplicationState()
    val useAuthentication = PropertiesConfig.Configuration().useAuthentication

    applicationLifeCycleConfig(applicationState)
    commonConfig()
    securityConfig(useAuthentication)
    routingConfig(useAuthentication, applicationState)
}

private class HttpServer(
    port: Int,
    private val databaseConfig: DatabaseConfig = DatabaseConfig(),
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
