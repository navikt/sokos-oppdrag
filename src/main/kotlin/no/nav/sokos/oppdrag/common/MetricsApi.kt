package no.nav.sokos.oppdrag.common

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.prometheus.client.exporter.common.TextFormat
import no.nav.sokos.oppdrag.common.Metrics as AppMetrics
import no.nav.sokos.oppdrag.oppdragsinfo.metrics.Metrics as OppdragsInfoMetrics

fun Routing.metricsApi() {
    route("internal") {
        get("metrics") {
            call.respondText(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                AppMetrics.prometheusMeterRegistry.scrape() + OppdragsInfoMetrics.prometheusMeterRegistryOppdragsInfo.scrape()
            }
        }
    }
}