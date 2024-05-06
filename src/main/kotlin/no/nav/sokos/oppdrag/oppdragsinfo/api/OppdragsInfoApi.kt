package no.nav.sokos.oppdrag.oppdragsinfo.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.oppdragsinfo.OppdragsInfoService
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.SokOppdragRequest

private const val BASE_PATH = "/api/v1/oppdragsinfo"

fun Route.oppdragsInfoApi(oppdragsInfoService: OppdragsInfoService = OppdragsInfoService()) {
    route(BASE_PATH) {
        post("oppdrag") {
            val oppdragsSokOppdragRequest = call.receive<SokOppdragRequest>()
            call.respond(
                oppdragsInfoService.sokOppdrag(
                    oppdragsSokOppdragRequest.gjelderId,
                    oppdragsSokOppdragRequest.fagGruppeKode,
                    call,
                ),
            )
        }

        get("faggrupper") {
            call.respond(
                oppdragsInfoService.hentFaggrupper(),
            )
        }

        post("{oppdragsId}") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            call.respond(
                oppdragsInfoService.hentOppdrag(
                    gjelderIdRequest.gjelderId,
                    call.parameters["oppdragsId"].orEmpty(),
                ),
            )
        }

        post("{oppdragsId}/omposteringer") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            call.respond(
                oppdragsInfoService.hentOppdragsOmposteringer(
                    gjelderIdRequest.gjelderId,
                    call.parameters["oppdragsId"].orEmpty(),
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

        get("{oppdragsId}/{linjeId}/status") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeStatuser(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/attestant") {
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

        get("{oppdragsId}/{linjeId}/valuta") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeValuta(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/skyldner") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeSkyldner(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kravhaver") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeKravhaver(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/enhet") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeEnheter(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/grad") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeGrad(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/tekst") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeTekst(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/kidliste") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeKidListe(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/maksdato") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeMaksdato(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }

        get("{oppdragsId}/{linjeId}/ovrig") {
            call.respond(
                oppdragsInfoService.hentOppdragsLinjeOvrig(
                    call.parameters["oppdragsId"].orEmpty(),
                    call.parameters["linjeId"].orEmpty(),
                ),
            )
        }
    }
}
