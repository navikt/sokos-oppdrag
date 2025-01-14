package no.nav.sokos.oppdrag.config

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing

import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoSwaggerApi as oppdragsinfoSwaggerApi
import no.nav.sokos.oppdrag.attestasjon.api.attestasjonApi
import no.nav.sokos.oppdrag.attestasjon.api.attestasjonSwaggerApi
import no.nav.sokos.oppdrag.fastedata.api.fastedataApi
import no.nav.sokos.oppdrag.integration.api.integrationApi
import no.nav.sokos.oppdrag.integration.api.integrationSwaggerApi
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi
import no.nav.sokos.oppdrag.venteregister.api.venteregisterApi

fun Application.routingConfig(
    useAuthentication: Boolean,
    applicationState: ApplicationState,
) {
    routing {
        internalNaisRoutes(applicationState)
        oppdragsinfoSwaggerApi()
        integrationSwaggerApi()
        attestasjonSwaggerApi()
        venteregisterApi()
        authenticate(useAuthentication, AUTHENTICATION_NAME) {
            integrationApi()
            oppdragsInfoApi()
            attestasjonApi()
            fastedataApi()
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
