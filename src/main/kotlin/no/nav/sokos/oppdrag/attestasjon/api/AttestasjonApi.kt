package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.common.model.GjelderIdRequestBody

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(service: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("gjeldersok") {
            val gjelderIdRequestBody = call.receive<GjelderIdRequestBody>()
            call.respond(
                service.hentOppdragForAttestering(
                    gjelderIdRequestBody.gjelderId,
                    call,
                ),
            )
        }

        get("oppdragslinjer/{oppdragsId}") {
            call.respond(
                service.hentOppdragslinjerForAttestering(
                    call.parameters["oppdragsId"].orEmpty().toInt(),
                ),
            )
        }
    }
}
