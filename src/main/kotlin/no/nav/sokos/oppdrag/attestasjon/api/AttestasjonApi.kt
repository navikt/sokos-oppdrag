package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.common.dto.toPaginatedDTO
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(attestasjonService: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("sok") {
            val request = call.receive<OppdragsRequest>()
            val saksbehandler = getSaksbehandler(call)
            val page = call.parameters["page"]?.toInt() ?: 1
            val rows = call.parameters["rows"]?.toInt() ?: 10
            val sortKey = call.parameters["sortKey"]

            val oppdragPair =
                attestasjonService.getOppdrag(
                    gjelderId = request.gjelderId,
                    fagSystemId = request.fagSystemId,
                    kodeFagGruppe = request.kodeFagGruppe,
                    kodeFagOmraade = request.kodeFagOmraade,
                    attestert = request.attestert,
                    sortKey = sortKey,
                    page,
                    rows,
                    saksbehandler,
                )

            call.respond(oppdragPair.first.toPaginatedDTO(page, rows, oppdragPair.second))
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
