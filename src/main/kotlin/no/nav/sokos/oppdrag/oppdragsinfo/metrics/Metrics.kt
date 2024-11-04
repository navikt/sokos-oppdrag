package no.nav.sokos.oppdrag.oppdragsinfo.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

private const val METRICS_NAMESPACE = "sokos_oppdrag_oppdragsinfo"

object Metrics {
    val prometheusMeterRegistryOppdragsInfo = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}
