package no.nav.sokos.oppdrag.integration.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.core.metrics.Counter

private const val METRICS_NAMESPACE = "sokos_oppdrag_integration"

private const val EREG_CALL_COUNTER = "${METRICS_NAMESPACE}_ereg_call_counter"
private const val TP_CALL_COUNTER = "${METRICS_NAMESPACE}_tp_call_counter"

object Metrics {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val eregCallCounter: Counter =
        Counter.builder()
            .name(EREG_CALL_COUNTER)
            .help("Counts calls to ereg with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val tpCallCounter: Counter =
        Counter.builder()
            .name(TP_CALL_COUNTER)
            .help("Counts calls to tp with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)
}
