package no.nav.sokos.oppdrag.oppdragsinfo.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.oppdragsInfoSwaggerApi() {
    swaggerUI(
        path = "api/v1/oppdragsinfo/docs",
        swaggerFile = "openapi/oppdragsinfo-v1-swagger.yaml",
    )
}
