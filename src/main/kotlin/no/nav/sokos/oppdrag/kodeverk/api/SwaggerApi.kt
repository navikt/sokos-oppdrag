package no.nav.sokos.oppdrag.kodeverk.api

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Routing

fun Routing.kodeverkSwaggerApi() {
    swaggerUI(
        path = "api/v1/kodeverk/docs",
        swaggerFile = "openapi/kodeverk-v1-swagger.yaml",
    )
}
