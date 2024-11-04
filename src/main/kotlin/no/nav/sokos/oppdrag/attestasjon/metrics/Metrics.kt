package no.nav.sokos.oppdrag.attestasjon.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

private const val METRICS_NAMESPACE = "sokos_oppdrag_attestasjon"

object Metrics {
    val prometheusMeterRegistryAttestasjon = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}
