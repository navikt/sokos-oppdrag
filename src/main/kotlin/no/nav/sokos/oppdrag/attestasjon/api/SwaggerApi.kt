package no.nav.sokos.oppdrag.attestasjon.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.attestasjonSwaggerApi() {
    swaggerUI(
        path = "api/v1/attestasjon/docs",
        swaggerFile = "openapi/attestasjon-v1-swagger.yaml",
    )
}
