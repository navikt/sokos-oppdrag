import com.ibm.mq.jakarta.jms.MQQueue
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.sokos.oppdrag.common.mq.JmsProducerService
import no.nav.sokos.oppdrag.config.MQConfig.connectionFactorySkattekort
import no.nav.sokos.oppdrag.config.PropertiesConfig

private val logger = KotlinLogging.logger {}

class SkattekortClientService(
    private val jmsProducer: JmsProducerService = JmsProducerService(connectionFactory = connectionFactorySkattekort()),
    private val sokosSkattekortQueue: Queue = MQQueue(PropertiesConfig.MqProperties().sokosSkattekortBestillingQueue),
) {
    fun bestillSkattekort(
        fnr: String,
        inntektsAar: Int,
    ) {
        logger.info { "Oppdragsinfo bestiller skattekort" }
        val response = jmsProducer.send("OS;$inntektsAar;$fnr", sokosSkattekortQueue)
    }
}
