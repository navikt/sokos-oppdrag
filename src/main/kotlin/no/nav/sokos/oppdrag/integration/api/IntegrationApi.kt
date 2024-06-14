package no.nav.sokos.oppdrag.integration.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.common.model.GjelderIdRequestBody
import no.nav.sokos.oppdrag.integration.service.IntegrationService

private const val BASE_PATH = "/api/v1/integration"

fun Route.integrationApi(integrationService: IntegrationService = IntegrationService()) {
    route(BASE_PATH) {
        post("hent-navn") {
            val gjelderIdRequestBody = call.receive<GjelderIdRequestBody>()
            call.respond(
                integrationService.hentNavnForGjelderId(
                    gjelderIdRequestBody.gjelderId,
                    call,
                ),
            )
        }
    }
}
