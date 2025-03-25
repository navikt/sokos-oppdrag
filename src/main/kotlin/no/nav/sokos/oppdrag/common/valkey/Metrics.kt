package no.nav.sokos.oppdrag.common.valkey

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

object Metrics {
    val prometheusMeterRegistryValkey = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}
