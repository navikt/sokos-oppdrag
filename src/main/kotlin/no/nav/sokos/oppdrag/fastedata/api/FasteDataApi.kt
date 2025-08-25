package no.nav.sokos.oppdrag.fastedata.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.fastedata.service.FasteDataService
import no.nav.sokos.oppdrag.fastedata.validator.validateFagomraadeQueryParameter

private const val BASE_PATH = "/api/v1/fastedata"

fun Route.fastedataApi(fasteDataService: FasteDataService = FasteDataService()) {
    route("$BASE_PATH/fagomraader") {
        get("") {
            call.respond(
                fasteDataService.getFagomraader(),
            )
        }
        get("{kodeFagomraade}/korrigeringsaarsaker") {
            call.respond(
                fasteDataService.getKorrigeringsaarsaker(
                    call.parameters["kodeFagomraade"].orEmpty().validateFagomraadeQueryParameter(),
                ),
            )
        }
        get("{kodeFagomraade}/bilagstyper") {
            call.respond(
                fasteDataService.getBilagstyper(
                    call.parameters["kodeFagomraade"].orEmpty().validateFagomraadeQueryParameter(),
                ),
            )
        }
        get("{kodeFagomraade}/klassekoder") {
            call.respond(
                fasteDataService.getKlassekoder(
                    call.parameters["kodeFagomraade"].orEmpty().validateFagomraadeQueryParameter(),
                ),
            )
        }
    }

    route("$BASE_PATH/ventestatuskoder") {
        get("") {
            call.respond(
                fasteDataService.getAllVentestatuskoder(),
            )
        }
    }

    route("$BASE_PATH/ventekriterier") {
        get("") {
            call.respond(
                fasteDataService.getAllVentekriterier(),
            )
        }
    }

    route("$BASE_PATH/faggrupper") {
        get("") {
            call.respond(
                fasteDataService.getFaggrupper(),
            )
        }
        get("{kodeFaggruppe}/redusertSkatt") {
            call.respond(
                fasteDataService.getRedusertSkatt(
                    call.parameters["kodeFaggruppe"].orEmpty(),
                ),
            )
        }
        get("{kodeFaggruppe}/kjoreplan") {
            call.respond(
                fasteDataService.getKjoreplan(
                    call.parameters["kodeFaggruppe"].orEmpty(),
                ),
            )
        }
    }

    route("$BASE_PATH/klassekoder") {
        get("") {
            call.respond(
                fasteDataService.getAllKlassekoder(),
            )
        }
    }
}
