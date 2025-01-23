package no.nav.sokos.oppdrag.fastedata.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.fastedataSwaggerApi() {
    swaggerUI(
        path = "api/v1/fastedata/docs",
        swaggerFile = "openapi/fastedata-v1-swagger.yaml",
    )
}
