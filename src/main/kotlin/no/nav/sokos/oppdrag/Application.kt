package no.nav.sokos.oppdrag

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import no.nav.sokos.oppdrag.config.ApplicationState
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.applicationLifecycleConfig
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.config.routingConfig
import no.nav.sokos.oppdrag.config.securityConfig

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(true)
}

fun Application.module() {
    val applicationState = ApplicationState()
    val useAuthentication = PropertiesConfig.Configuration().useAuthentication

    applicationLifecycleConfig(applicationState)
    commonConfig()
    securityConfig(useAuthentication)
    routingConfig(useAuthentication, applicationState)
}
