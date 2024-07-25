package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.attestasjon.domain.SokAttestasjonRequestBody
import no.nav.sokos.oppdrag.attestasjon.model.AttestasjondetaljerRequestBody
import no.nav.sokos.oppdrag.attestasjon.service.AttestasjonService
import no.nav.sokos.oppdrag.common.model.GjelderIdRequest

private const val BASE_PATH = "/api/v1/attestasjon"

fun Route.attestasjonApi(service: AttestasjonService = AttestasjonService()) {
    route(BASE_PATH) {
        post("gjeldersok") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            call.respond(
                service.hentOppdragForAttestering(
                    gjelderId = gjelderIdRequest.gjelderId,
                    applicationCall = call,
                ),
            )
        }

        post("sok") {
            val sokAttestasjonRequestBody = call.receive<SokAttestasjonRequestBody>()
            call.respond(
                service.hentOppdragForAttestering(
                    gjelderId = sokAttestasjonRequestBody.gjelderId,
                    fagsystemId = sokAttestasjonRequestBody.fagsystemId,
                    kodeFaggruppe = sokAttestasjonRequestBody.kodeFaggruppe,
                    kodeFagomraade = sokAttestasjonRequestBody.kodeFagomraade,
                    attestert = sokAttestasjonRequestBody.attestert,
                    applicationCall = call,
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

        get("fagomraader") {
            call.respond(
                service.hentFagomraader(),
            )
        }

        post("oppdragslinjer") {
            val sokAttestasjonRequestBody = call.receive<AttestasjondetaljerRequestBody>()
            call.respond(
                service.hentListeMedOppdragslinjerForAttestering(
                    sokAttestasjonRequestBody.oppdragsIder,
                ),
            )
        }
    }
}
