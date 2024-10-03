package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(attestasjonService: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("sok") {
            val request = call.receive<OppdragsRequest>()
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                attestasjonService.getOppdrag(
                    request.gjelderId,
                    request.fagSystemId,
                    request.kodeFagGruppe,
                    request.kodeFagOmraade,
                    request.attestert,
                    saksbehandler,
                ),
            )
        }

        get("fagomraader") {
            call.respond(
                attestasjonService.getFagOmraade(),
            )
        }

        get("{oppdragsId}/oppdragsdetaljer") {
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                attestasjonService.getOppdragsdetaljer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                    saksbehandler,
                ),
            )
        }

        post("attestere") {
            val request = call.receive<AttestasjonRequest>()
            val saksbehandler = getSaksbehandler(call)
            call.respond(
                attestasjonService.attestereOppdrag(request, saksbehandler),
            )
        }
    }
}
