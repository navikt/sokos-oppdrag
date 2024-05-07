package no.nav.sokos.oppdrag.common.config

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import no.nav.sokos.oppdrag.ApplicationState
import no.nav.sokos.oppdrag.common.naisApi
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi
import no.nav.sokos.oppdrag.oppdragsinfo.api.swaggerApi as oppdragsinfoSwaggerApi

fun Application.routingConfig(
    applicationState: ApplicationState,
    useAuthentication: Boolean,
) {
    routing {
        naisApi({ applicationState.initialized }, { applicationState.running })
        oppdragsinfoSwaggerApi()
        authenticate(useAuthentication, AUTHENTICATION_NAME) {
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
