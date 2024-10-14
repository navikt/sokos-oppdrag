package no.nav.sokos.oppdrag.config

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import kotlinx.serialization.json.Json
import no.nav.sokos.oppdrag.attestasjon.config.requestValidationAttestasjonConfig
import no.nav.sokos.oppdrag.integration.config.requestValidationIntegrationConfig
import no.nav.sokos.oppdrag.integration.metrics.Metrics
import no.nav.sokos.oppdrag.oppdragsinfo.config.requestValidationOppdragsInfoConfig
import org.slf4j.event.Level
import java.util.UUID
import no.nav.sokos.oppdrag.integration.metrics.Metrics as AppMetrics
import no.nav.sokos.oppdrag.oppdragsinfo.metrics.Metrics as OppdragsInfoMetrics

const val SECURE_LOGGER = "secureLogger"
const val AUDIT_LOGGER = "auditLogger"

fun Application.commonConfig() {
    install(CallId) {
        header(HttpHeaders.XCorrelationId)
        generate { UUID.randomUUID().toString() }
        verify { callId: String -> callId.isNotEmpty() }
    }
    install(CallLogging) {
        level = Level.INFO
        callIdMdc(HttpHeaders.XCorrelationId)
        filter { call -> call.request.path().startsWith("/api") }
        disableDefaultColors()
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            },
        )
    }
    install(StatusPages) {
        statusPageConfig()
    }
    install(RequestValidation) {
        requestValidationIntegrationConfig()
        requestValidationOppdragsInfoConfig()
        requestValidationAttestasjonConfig()
    }
    install(MicrometerMetrics) {
        registry = Metrics.prometheusMeterRegistry
        meterBinders =
            listOf(
                UptimeMetrics(),
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                JvmThreadMetrics(),
                ProcessorMetrics(),
            )
    }
}

fun Routing.internalNaisRoutes(
    applicationState: ApplicationState,
    readynessCheck: () -> Boolean = { applicationState.ready },
    alivenessCheck: () -> Boolean = { applicationState.alive },
) {
    route("internal") {
        get("isAlive") {
            when (alivenessCheck()) {
                true -> call.respondText { "I'm alive :)" }
                else ->
                    call.respondText(
                        text = "I'm dead x_x",
                        status = HttpStatusCode.InternalServerError,
                    )
            }
        }
        get("isReady") {
            when (readynessCheck()) {
                true -> call.respondText { "I'm ready! :)" }
                else ->
                    call.respondText(
                        text = "Wait! I'm not ready yet! :O",
                        status = HttpStatusCode.InternalServerError,
                    )
            }
        }
        get("metrics") {
            call.respondText(AppMetrics.prometheusMeterRegistry.scrape() + OppdragsInfoMetrics.prometheusMeterRegistryOppdragsInfo.scrape())
        }
    }
}
