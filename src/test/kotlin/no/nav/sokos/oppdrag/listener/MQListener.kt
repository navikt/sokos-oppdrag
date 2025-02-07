package no.nav.sokos.oppdrag.listener

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.mockk.mockk
import jakarta.jms.ConnectionFactory
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
    val senderQueueMock = mockk<ActiveMQQueue>()
    val replyQueueMock = mockk<ActiveMQQueue>()

    override suspend fun beforeSpec(spec: Spec) {
        server.start()
        connectionFactory = ActiveMQConnectionFactory("vm:localhost?create=false")
    }

    override suspend fun afterSpec(spec: Spec) {
        server.stop()
    }
}
