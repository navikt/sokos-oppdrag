package no.nav.sokos.oppdrag.fastedata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.fastedata.service.FasteDataService
import no.nav.sokos.oppdrag.fastedata.validator.validateFagomraade

private const val BASE_PATH = "/api/v1/fastedata"

fun Route.fastedataApi(fasteDataService: FasteDataService = FasteDataService()) {
    route("$BASE_PATH/fagomraader") {
        get("") {
            call.respond(
                fasteDataService.getFagomraader(),
            )
        }
        get("{kodeFagomraade}/korrigeringsaarsaker") {
            val kodeFagomraade = call.parameters["kodeFagomraade"].orEmpty()
            if (!validateFagomraade(kodeFagomraade)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                )
            }
            print(kodeFagomraade)
            call.respond(
                fasteDataService.getKorrigeringsaarsaker(
                    call.parameters["kodeFagomraade"].orEmpty(),
                ),
            )
        }
        get("{kodeFagomraade}/bilagstyper") {
            val kodeFagomraade = call.parameters["kodeFagomraade"].orEmpty()
            if (!validateFagomraade(kodeFagomraade)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                )
            }
            call.respond(
                fasteDataService.getBilagstyper(
                    call.parameters["kodeFagomraade"].orEmpty(),
                ),
            )
        }
        get("{kodeFagomraade}/klassekoder") {
            val kodeFagomraade = call.parameters["kodeFagomraade"].orEmpty()
            if (!validateFagomraade(kodeFagomraade)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                )
            }
            call.respond(
                fasteDataService.getKlassekoder(
                    kodeFagomraade,
                ),
            )
        }
    }
    route("$BASE_PATH/faggrupper") {
        get("") {
            call.respond(
                HttpStatusCode.NotImplemented,
            )
        }
        get("{kodeFaggruppe}/redusertSkatt") {
            call.respond(
                HttpStatusCode.NotImplemented,
            )
        }
    }

    route("$BASE_PATH/klassekoder") {
        get("") {
            call.respond(
                HttpStatusCode.NotImplemented,
            )
        }
    }
}
