package no.nav.sokos.oppdrag

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import no.nav.sokos.oppdrag.config.ApplicationState
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.applicationLifeCycleConfig
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.config.routingConfig
import no.nav.sokos.oppdrag.config.securityConfig

fun main() {
    val applicationEngineEnvironment =
        applicationEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
        }

    val server =
        embeddedServer(
            factory = Netty,
            environment = applicationEngineEnvironment,
            configure = {
                connector {
                    port = 8080
                }
                shutdownGracePeriod = 2000
                shutdownTimeout = 10000
            },
            module = Application::module,
        )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(2000, 10000)
        },
    )
    server.start(true)
}

fun Application.module() {
    val applicationState = ApplicationState()
    val useAuthentication = PropertiesConfig.Configuration().useAuthentication

    applicationLifeCycleConfig(applicationState)
    commonConfig()
    securityConfig(useAuthentication)
    routingConfig(useAuthentication, applicationState)
}
