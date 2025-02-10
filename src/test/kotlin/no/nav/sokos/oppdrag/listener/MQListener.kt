package no.nav.sokos.oppdrag.listener

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldNotBe
import jakarta.jms.ConnectionFactory
import jakarta.jms.Queue
import jakarta.jms.Session
import jakarta.jms.TextMessage
import org.apache.activemq.artemis.api.core.TransportConfiguration
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.apache.activemq.artemis.jms.client.ActiveMQQueue

object MQListener : TestListener {
    private val server =
        EmbeddedActiveMQ()
            .setConfiguration(
                ConfigurationImpl()
                    .setPersistenceEnabled(false)
                    .setSecurityEnabled(false)
                    .addAcceptorConfiguration(TransportConfiguration(InVMAcceptorFactory::class.java.name)),
            )

    lateinit var connectionFactory: ConnectionFactory
    val senderQueue: Queue = ActiveMQQueue("testQueue")

    override suspend fun beforeSpec(spec: Spec) {
        server.start()
        connectionFactory = ActiveMQConnectionFactory("vm:localhost?create=false")
    }

    override suspend fun afterSpec(spec: Spec) {
        server.stop()
    }

    fun setupMQReply(
        senderQueue: Queue,
        replyMessage: String,
    ) {
        val jmsContext = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)
        Thread {
            jmsContext.createConsumer(senderQueue).use { consumer ->
                println("get the message")
                val producer = jmsContext.createProducer()
                val message = consumer.receive() as TextMessage
                message.text shouldNotBe null

                producer.send(message.jmsReplyTo, jmsContext.createTextMessage(replyMessage))
            }
        }.start()
    }
}
