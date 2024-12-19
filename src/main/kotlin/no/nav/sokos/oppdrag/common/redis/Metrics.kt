package no.nav.sokos.oppdrag.common.redis

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

object Metrics {
    val prometheusMeterRegistryRedis = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}
