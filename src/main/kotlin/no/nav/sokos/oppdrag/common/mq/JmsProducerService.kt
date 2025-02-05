package no.nav.sokos.oppdrag.common.mq

import com.ibm.msg.client.jakarta.jms.JmsConstants.AUTO_ACKNOWLEDGE
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSContext
import jakarta.jms.JMSException
import jakarta.jms.JMSProducer
import jakarta.jms.Queue
import jakarta.jms.TextMessage

import no.nav.sokos.oppdrag.config.MQConfig

open class JmsProducerService(
    connectionFactory: ConnectionFactory = MQConfig.connectionFactory(),
) {
    private val jmsContext: JMSContext = connectionFactory.createContext()

    open fun send(
        payload: String,
        senderQueue: Queue,
    ): String {
        jmsContext.createContext(AUTO_ACKNOWLEDGE).use { context ->
            val temporaryQueue = context.createTemporaryQueue()
            try {
                val producer: JMSProducer = context.createProducer().apply { jmsReplyTo = temporaryQueue }
                producer.send(senderQueue, payload)

                context.createConsumer(temporaryQueue).use { consumer ->
                    val inputMessageText =
                        when (val consumedMessage = consumer.receive(20000)) {
                            is TextMessage -> consumedMessage.text
                            else ->
                                throw JMSException(
                                    "Innkommende melding må være en byte-melding eller tekst, men var av typen: " +
                                        consumedMessage.jmsType,
                                )
                        }
                    return inputMessageText
                }
            } catch (exception: Exception) {
                throw JMSException(exception.message ?: "Feil ved sending av melding til MQ-kø")
            } finally {
                temporaryQueue.delete()
            }
        }
    }
}
