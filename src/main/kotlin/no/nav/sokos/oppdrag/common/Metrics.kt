package no.nav.sokos.oppdrag.common

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.Counter

private const val METRICS_NAMESPACE = "sokos_oppdrag"

object Metrics {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val appStateRunningFalse: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("app_state_running_false")
            .help("app state running changed to false")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val appStateReadyFalse: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("app_state_ready_false")
            .help("app state ready changed to false")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val eregCallCounter: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("ereg_call_counter")
            .labelNames("responseCode")
            .help("Counts calls to ereg with response status code")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val tpCallCounter: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("tp_call_counter")
            .labelNames("responseCode")
            .help("Counts calls to tp with response status code")
            .register(prometheusMeterRegistry.prometheusRegistry)
}
