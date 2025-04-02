package no.nav.sokos.oppdrag.integration.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.core.metrics.Counter

private const val METRICS_NAMESPACE = "sokos_oppdrag_integration"

private const val EREG_CALL_COUNTER = "${METRICS_NAMESPACE}_ereg_call_counter"
private const val SAMHANDLER_COUNTER = "${METRICS_NAMESPACE}_samhandler_call_counter"
private const val PDL_CALL_COUNTER = "${METRICS_NAMESPACE}_pdl_call_counter"
private const val NOM_CALL_COUNTER = "${METRICS_NAMESPACE}_nom_call_counter"

object Metrics {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val eregCallCounter: Counter =
        Counter
            .builder()
            .name(EREG_CALL_COUNTER)
            .help("Counts calls to Ereg with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val pdlCallCounter: Counter =
        Counter
            .builder()
            .name(PDL_CALL_COUNTER)
            .help("Counts calls to Pdl with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val nomCallCounter: Counter =
        Counter
            .builder()
            .name(NOM_CALL_COUNTER)
            .help("Counts calls to Nom with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)

    val samhandlerCounter: Counter =
        Counter
            .builder()
            .name(SAMHANDLER_COUNTER)
            .help("Counts calls to Samhandler with response status code")
            .withoutExemplars()
            .labelNames("responseCode")
            .register(prometheusMeterRegistry.prometheusRegistry)
}
