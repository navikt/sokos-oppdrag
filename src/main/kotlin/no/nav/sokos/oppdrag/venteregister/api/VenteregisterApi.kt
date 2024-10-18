package no.nav.sokos.oppdrag.venteregister.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.venteregister.service.VenteregisterService

private const val BASE_PATH = "/api/v1/venteregister"

fun Route.venteregisterApi(venteregisterService: VenteregisterService = VenteregisterService()) {
    route(BASE_PATH) {
        get("ansatte") {
            call.respond(
                venteregisterService.getAnsatte(),
            )
        }
    }
}
