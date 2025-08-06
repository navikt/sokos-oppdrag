package no.nav.sokos.oppdrag.config

import com.ibm.mq.constants.MQConstants
import com.ibm.msg.client.jakarta.jms.JmsConstants.JAKARTA_WMQ_PROVIDER
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import jakarta.jms.ConnectionFactory

private const val UTF_8_WITH_PUA = 1208

object MQConfig {
    fun connectionFactory(mqProperties: PropertiesConfig.MqProperties = PropertiesConfig.MqProperties()): ConnectionFactory =
        JmsFactoryFactory.getInstance(JAKARTA_WMQ_PROVIDER).createConnectionFactory().apply {
            setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT)
            setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, mqProperties.mqQueueManagerName)
            setStringProperty(WMQConstants.WMQ_HOST_NAME, mqProperties.mqHostname)
            setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, PropertiesConfig.Configuration().naisAppName)
            setIntProperty(WMQConstants.WMQ_PORT, mqProperties.mqPort)
            setStringProperty(WMQConstants.WMQ_CHANNEL, mqProperties.mqChannelName)
            setIntProperty(WMQConstants.WMQ_CCSID, UTF_8_WITH_PUA)
            setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE)
            setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)
            setStringProperty(WMQConstants.USERID, mqProperties.mqServiceUsername)
            setStringProperty(WMQConstants.PASSWORD, mqProperties.mqServicePassword)
        }
}
