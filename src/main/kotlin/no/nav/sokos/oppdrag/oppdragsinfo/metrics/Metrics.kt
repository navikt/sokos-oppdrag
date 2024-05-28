package no.nav.sokos.oppdrag.oppdragsinfo.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.core.metrics.Counter

private const val METRICS_NAMESPACE = "sokos_oppdrag_oppdragsinfo"

private const val DATABASE_FAILURE_COUNTER = "${METRICS_NAMESPACE}_database_failure_counter"

object Metrics {
    val prometheusMeterRegistryOppdragsInfo = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val databaseFailureCounterOppdragsInfo: Counter =
        Counter.builder()
            .name(DATABASE_FAILURE_COUNTER)
            .help("Count database errors")
            .withoutExemplars()
            .labelNames("errorCode", "sqlState")
            .register(prometheusMeterRegistryOppdragsInfo.prometheusRegistry)
}
