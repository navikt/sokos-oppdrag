package no.nav.sokos.oppdrag.kodeverk.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.kodeverk.service.KodeverkService

private const val BASE_PATH = "/api/v1/kodeverk"

fun Route.kodeverkApi(kodeverkService: KodeverkService = KodeverkService()) {
    route(BASE_PATH) {
        get("fagomraader") {
            call.respond(
                kodeverkService.getFagOmraader(),
            )
        }

        get("faggrupper") {
            call.respond(
                kodeverkService.getFagGrupper(),
            )
        }
    }
}
