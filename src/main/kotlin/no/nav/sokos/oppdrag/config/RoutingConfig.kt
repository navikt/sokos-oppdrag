package no.nav.sokos.oppdrag.config

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import no.nav.sokos.oppdrag.integration.api.integrationApi
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi
import no.nav.sokos.oppdrag.oppdragsinfo.api.swaggerApi as oppdragsinfoSwaggerApi

fun Application.routingConfig(
    useAuthentication: Boolean,
    applicationState: ApplicationState,
) {
    routing {
        internalNaisRoutes(applicationState)
        oppdragsinfoSwaggerApi()
        authenticate(useAuthentication, AUTHENTICATION_NAME) {
            integrationApi()
            oppdragsInfoApi()
        }
    }
}

fun Route.authenticate(
    useAuthentication: Boolean,
    authenticationProviderId: String? = null,
    block: Route.() -> Unit,
) {
    if (useAuthentication) authenticate(authenticationProviderId) { block() } else block()
}
