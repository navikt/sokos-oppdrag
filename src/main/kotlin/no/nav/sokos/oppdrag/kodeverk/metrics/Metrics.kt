package no.nav.sokos.oppdrag.kodeverk.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

private const val METRICS_NAMESPACE = "sokos_oppdrag_kodeverk"

object Metrics {
    val prometheusMeterRegistryKodeverk = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}
