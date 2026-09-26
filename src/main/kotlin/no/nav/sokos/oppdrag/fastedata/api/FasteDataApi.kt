package no.nav.sokos.oppdrag.fastedata.api

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.fastedata.api.parameter.KodeFagOmraadePathParameter
import no.nav.sokos.oppdrag.fastedata.api.parameter.KodeFaggruppePathParameter
import no.nav.sokos.oppdrag.fastedata.api.parameter.validateKodeFagOmraade
import no.nav.sokos.oppdrag.fastedata.api.parameter.validateKodeFaggruppe
import no.nav.sokos.oppdrag.fastedata.service.FasteDataService

private const val BASE_PATH = "/api/v1/fastedata"

private fun ApplicationCall.validertKodeFagOmraade(): String =
    KodeFagOmraadePathParameter(parameters["kodeFagomraade"].orEmpty())
        .also { it.validateKodeFagOmraade() }
        .kodeFagOmraade

private fun ApplicationCall.validertKodeFaggruppe(): String =
    KodeFaggruppePathParameter(parameters["kodeFaggruppe"].orEmpty())
        .also { it.validateKodeFaggruppe() }
        .kodeFaggruppe

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
                    call.validertKodeFagOmraade(),
                ),
            )
        }
        get("{kodeFagomraade}/bilagstyper") {
            call.respond(
                fasteDataService.getBilagstyper(
                    call.validertKodeFagOmraade(),
                ),
            )
        }
        get("{kodeFagomraade}/klassekoder") {
            call.respond(
                fasteDataService.getKlassekoder(
                    call.validertKodeFagOmraade(),
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
        get("{kodeFaggruppe}/fagomraader") {
            call.respond(
                fasteDataService.getFagomraaderForFaggruppe(
                    call.validertKodeFaggruppe(),
                ),
            )
        }
        get("{kodeFaggruppe}/redusertSkatt") {
            call.respond(
                fasteDataService.getRedusertSkatt(
                    call.validertKodeFaggruppe(),
                ),
            )
        }
        get("{kodeFaggruppe}/kjoreplan") {
            call.respond(
                fasteDataService.getKjoreplan(
                    call.validertKodeFaggruppe(),
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

    route("$BASE_PATH/trekkgrupper") {
        get("") {
            call.respond(
                fasteDataService.getTrekkgrupper(),
            )
        }
    }

    route("$BASE_PATH/trekkregler") {
        get("") {
            call.respond(
                fasteDataService.getTrekkregler(),
            )
        }
        get("{kodeTrekktype}/kjoreplan") {
            call.respond(
                fasteDataService.getKjoreplanTrekk(
                    call.parameters["kodeTrekktype"].orEmpty(),
                ),
            )
        }
    }
}
