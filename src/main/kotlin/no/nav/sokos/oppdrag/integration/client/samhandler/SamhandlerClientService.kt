package no.nav.sokos.oppdrag.integration.client.samhandler

import java.io.StringReader
import java.io.StringWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

import com.ibm.mq.jakarta.jms.MQQueue
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import jakarta.jms.Queue
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller

import no.nav.sokos.oppdrag.common.mq.JmsProducerService
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.tss.SamhandlerIDataB980Type
import no.nav.tss.TServicerutiner
import no.nav.tss.TssSamhandlerData

class SamhandlerClientService(
    private val jmsProducerService: JmsProducerService = JmsProducerService(),
    private val tssQueue: Queue =
        MQQueue(PropertiesConfig.MqProperties().mqTssSamhandlerServiceQueue).apply {
            targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ
        },
) {
    private val jaxbContextSamhandler = JAXBContext.newInstance(TssSamhandlerData::class.java)

    fun getSamhandler(tssId: String): String {
        val request =
            TssSamhandlerData().apply {
                tssInputData =
                    TssSamhandlerData.TssInputData().apply {
                        tssServiceRutine =
                            TServicerutiner().apply {
                                samhandlerIDataB980 =
                                    SamhandlerIDataB980Type().apply {
                                        idOffTSS = tssId
                                        hentNavn = "J"
                                        brukerID = "OB04"
                                    }
                            }
                    }
            }

        val requestXML = marshallTssRequest(request)
        val response = jmsProducerService.send(requestXML, tssQueue)

        return unmarshallTssResponse(response)
            .tssOutputData.samhandlerODataB980.ident
            .first()
            ?.navnSamh ?: throw SamhanderException("Samhandler med tssId: $tssId ikke funnet")
    }

    private fun marshallTssRequest(request: Any): String {
        val marshaller =
            jaxbContextSamhandler.createMarshaller().apply {
                setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            }
        return StringWriter().use {
            marshaller.marshal(request, it)
            it.toString()
        }
    }

    private fun unmarshallTssResponse(response: String): TssSamhandlerData =
        jaxbContextSamhandler
            .createUnmarshaller()
            .unmarshal(
                XMLInputFactory.newInstance().createXMLStreamReader(StreamSource(StringReader(response))),
                TssSamhandlerData::class.java,
            ).value
}
