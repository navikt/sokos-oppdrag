package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsIdRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(service: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("sok") {
            val oppdragsRequest = call.receive<OppdragsRequest>()
            call.respond(
                service.getOppdrag(
                    gjelderId = oppdragsRequest.gjelderId,
                    fagsystemId = oppdragsRequest.fagsystemId,
                    kodeFagGruppe = oppdragsRequest.kodeFagGruppe,
                    kodeFagOmraade = oppdragsRequest.kodeFagOmraade,
                    attestert = oppdragsRequest.attestert,
                    applicationCall = call,
                ),
            )
        }

        get("fagomraader") {
            call.respond(
                service.getFagOmraade(),
            )
        }

        get("oppdragsdetaljer/{oppdragsId}") {
            call.respond(
                service.getOppdragsDetaljer(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }
    }
}
