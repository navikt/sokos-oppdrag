package no.nav.sokos.oppdrag.fastedata.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.fastedata.service.FasteDataService

private const val BASE_PATH = "/api/v1/fastedata"

fun Route.fastedataApi(fasteDataService: FasteDataService = FasteDataService()) {
    route(BASE_PATH) {
        get("fagomraader") {
            call.respond(
                fasteDataService.getFagomraader(),
            )
        }
        get("{kodeFagomraade}/korrigeringsaarsaker") {
            call.respond(
                fasteDataService.getKorrigeringsaarsaker(
                    call.parameters["kodeFagomraade"].orEmpty(),
                ),
            )
        }
        get("{kodeFagomraade}/bilagstyper") {
            call.respond(
                fasteDataService.getBilagstyper(
                    call.parameters["kodeFagomraade"].orEmpty(),
                ),
            )
        }
        get("{kodeFagomraade}/klassekoder") {
            call.respond(
                fasteDataService.getKlassekoder(
                    call.parameters["kodeFagomraade"].orEmpty(),
                ),
            )
        }
    }
}
