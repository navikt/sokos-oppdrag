package no.nav.sokos.oppdrag.integration.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.integration.service.NameService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private const val BASE_PATH = "/api/v1/integration"

fun Route.integrationApi(nameService: NameService = NameService()) {
    route(BASE_PATH) {
        post("hentnavn") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                nameService.getNavn(
                    gjelderIdRequest.gjelderId,
                    saksbehandler,
                ),
            )
        }
    }
}
