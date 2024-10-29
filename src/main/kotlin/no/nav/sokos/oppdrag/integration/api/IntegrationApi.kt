package no.nav.sokos.oppdrag.integration.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.integration.service.IntegrationService
import no.nav.sokos.oppdrag.integration.tp.TpClientService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private const val BASE_PATH = "/api/v1/integration"

fun Route.integrationApi(
    integrationService: IntegrationService = IntegrationService(),
    tpService: TpClientService = TpClientService(),
) {
    route(BASE_PATH) {
        post("hentnavn") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                integrationService.getNavnForGjelderId(
                    gjelderIdRequest.gjelderId,
                    saksbehandler,
                ),
            )
        }

        post("testnom") {
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                tpService.getLeverandorNavn("80000111999"),
            )
        }
    }
}
