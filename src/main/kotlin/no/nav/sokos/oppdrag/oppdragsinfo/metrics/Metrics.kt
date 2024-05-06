package no.nav.sokos.oppdrag.oppdragsinfo.metrics

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.Counter

private const val METRICS_NAMESPACE = "sokos_oppdrag_oppdragsinfo"

object Metrics {
    val prometheusMeterRegistryOppdragsInfo = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val databaseFailureCounterOppdragsInfo: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("database_failure_counter")
            .labelNames("errorCode", "sqlState")
            .help("Count database errors")
            .register(prometheusMeterRegistryOppdragsInfo.prometheusRegistry)

    val eregCallCounter: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("ereg_call_counter")
            .labelNames("responseCode")
            .help("Counts calls to ereg with response status code")
            .register(prometheusMeterRegistryOppdragsInfo.prometheusRegistry)

    val tpCallCounter: Counter =
        Counter.build()
            .namespace(METRICS_NAMESPACE)
            .name("tp_call_counter")
            .labelNames("responseCode")
            .help("Counts calls to tp with response status code")
            .register(prometheusMeterRegistryOppdragsInfo.prometheusRegistry)
}
