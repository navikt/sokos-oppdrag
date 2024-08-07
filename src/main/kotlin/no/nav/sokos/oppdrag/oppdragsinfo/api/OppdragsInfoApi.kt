package no.nav.sokos.oppdrag.oppdragsinfo.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.oppdragsinfo.service.OppdragsInfoService

private const val BASE_PATH = "/api/v1/oppdragsinfo"

fun Route.oppdragsInfoApi(oppdragsInfoService: OppdragsInfoService = OppdragsInfoService()) {
    route(BASE_PATH) {
        post("sok") {
            val request = call.receive<OppdragsRequest>()
            call.respond(
                oppdragsInfoService.hentOppdragsEgenskaper(
                    request.gjelderId,
                    request.fagGruppeKode,
                    call,
                ),
            )
        }

        get("faggrupper") {
            call.respond(
                oppdragsInfoService.hentFagGrupper(),
            )
        }

        get("{oppdragsId}/oppdragslinjer") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/enheter") {
            call.respond(
                oppdragsInfoService.hentBehandlendeEnhetForOppdrag(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/omposteringer") {
            call.respond(
                oppdragsInfoService.hentOppdragsOmposteringer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/enhetshistorikk") {
            call.respond(
                oppdragsInfoService.hentOppdragsEnhetsHistorikk(
                    call.parameters["oppdragsId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/statushistorikk") {
            call.respond(
                oppdragsInfoService.hentOppdragsStatusHistorikk(
                    call.parameters["oppdragsId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/statuser") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeStatuser(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/attestanter") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeAttestanter(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/detaljer") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeDetaljer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/valutaer") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeValutaer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/skyldnere") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeSkyldnere(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kravhavere") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeKravhavere(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/enheter") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeEnheter(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/grader") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeGrader(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/tekster") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeTekster(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kid") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeKid(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/maksdatoer") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeMaksDatoer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/ovrig") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeOvriger(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }
    }
}
