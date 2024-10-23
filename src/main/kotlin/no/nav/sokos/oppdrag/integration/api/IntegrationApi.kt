package no.nav.sokos.oppdrag.integration.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.integration.service.IntegrationService
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetClientImpl
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler
import java.time.ZonedDateTime

private const val BASE_PATH = "/api/v1/integration"

fun Route.integrationApi(
    integrationService: IntegrationService = IntegrationService(),
    skjermetService: SkjermetService = SkjermetService(SkjermetClientImpl()),
) {
    route(BASE_PATH) {
        post("hentnavn") {
            val gjelderIdRequest = call.receive<GjelderIdRequest>()
            val saksbehandler = getSaksbehandler(call)
            if (skjermetService.kanSaksbehandlerSePerson(gjelderIdRequest.gjelderId, saksbehandler)) {
                call.respond(
                    integrationService.getNavnForGjelderId(
                        gjelderIdRequest.gjelderId,
                        saksbehandler,
                    ),
                )
            } else {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    ApiError(
                        ZonedDateTime.now(),
                        HttpStatusCode.Forbidden.value,
                        "Person er skjermet",
                        "Har forsøkt å gjøre et oppslag på navnet til skjermet person",
                        "$BASE_PATH/hentnavn",
                    ),
                )
            }
        }
    }
}
