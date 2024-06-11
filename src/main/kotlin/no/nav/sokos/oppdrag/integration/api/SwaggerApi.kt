package no.nav.sokos.oppdrag.integration.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.integrationSwaggerApi() {
    swaggerUI(
        path = "api/v1/integration/docs",
        swaggerFile = "openapi/integration-v1-swagger.yaml",
    )
}
