package no.nav.sokos.oppdrag

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.sokos.oppdrag.common.config.ApplicationState
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.applicationLifeCycleConfig
import no.nav.sokos.oppdrag.common.config.commonConfig
import no.nav.sokos.oppdrag.common.config.routingConfig
import no.nav.sokos.oppdrag.common.config.securityConfig

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(true)
}

fun Application.module() {
    val applicationState = ApplicationState()
    val useAuthentication = PropertiesConfig.Configuration().useAuthentication

    applicationLifeCycleConfig(applicationState)
    commonConfig()
    securityConfig(useAuthentication)
    routingConfig(useAuthentication, applicationState)
}
