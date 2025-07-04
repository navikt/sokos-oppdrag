package no.nav.sokos.oppdrag.venteregister.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.venteregisterSwaggerApi() {
    swaggerUI(
        path = "api/v1/venteregister/docs",
        swaggerFile = "openapi/venteregister-v1-swagger.yaml",
    )
}
