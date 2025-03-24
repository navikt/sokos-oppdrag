package no.nav.sokos.oppdrag.oppdragsinfo.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

import no.nav.sokos.oppdrag.oppdragsinfo.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.oppdragsinfo.service.OppdragsInfoService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private const val BASE_PATH = "/api/v1/oppdragsinfo"

fun Route.oppdragsInfoApi(oppdragsInfoService: OppdragsInfoService = OppdragsInfoService()) {
    route(BASE_PATH) {
        post("sok") {
            val request = call.receive<OppdragsRequest>()
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                oppdragsInfoService.getOppdrag(
                    request.gjelderId,
                    request.fagGruppeKode,
                    saksbehandler,
                ),
            )
        }

        get("{oppdragsId}/oppdragslinjer") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/enheter") {
            call.respond(
                oppdragsInfoService.getBehandlendeEnhetForOppdrag(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/omposteringer") {
            call.respond(
                oppdragsInfoService.getOppdragsOmposteringer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        get("{oppdragsId}/enhetshistorikk") {
            call.respond(
                oppdragsInfoService.getOppdragsEnhetsHistorikk(
                    call.parameters["oppdragsId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/statushistorikk") {
            call.respond(
                oppdragsInfoService.getOppdragsStatusHistorikk(
                    call.parameters["oppdragsId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/statuser") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeStatuser(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/attestanter") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeAttestanter(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/detaljer") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeDetaljer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/valutaer") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeValutaer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/skyldnere") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeSkyldnere(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kravhavere") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeKravhavere(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/enheter") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeEnheter(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/grader") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeGrader(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/tekster") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeTekster(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kid") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeKid(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/maksdatoer") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeMaksDatoer(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/ovrig") {
            call.respond(
                oppdragsInfoService.getOppdragsLinjeOvriger(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }
    }
}
