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

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(attestasjonService: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("sok") {
            val request = call.receive<OppdragsRequest>()
            call.respond(
                attestasjonService.getOppdrag(
                    gjelderId = request.gjelderId,
                    fagsystemId = request.fagsystemId,
                    kodeFagGruppe = request.kodeFagGruppe,
                    kodeFagOmraade = request.kodeFagOmraade,
                    attestert = request.attestert,
                    applicationCall = call,
                ),
            )
        }

        get("fagomraader") {
            call.respond(
                attestasjonService.getFagOmraade(),
            )
        }

        get("oppdragsdetaljer/{oppdragsId}") {
            call.respond(
                attestasjonService.getOppdragsDetaljer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }

        post("attestere") {
            val request = call.receive<AttestasjonRequest>()
            call.respond(
                attestasjonService.attestereOppdrag(request),
            )
        }
    }
}
