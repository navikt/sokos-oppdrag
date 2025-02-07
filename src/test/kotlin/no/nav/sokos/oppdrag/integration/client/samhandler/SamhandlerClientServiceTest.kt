package no.nav.sokos.oppdrag.integration.client.samhandler

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.common.mq.JmsProducerService
import no.nav.sokos.oppdrag.listener.MQListener
import no.nav.sokos.oppdrag.listener.MQListener.connectionFactory
import no.nav.sokos.oppdrag.listener.MQListener.senderQueue

class SamhandlerClientServiceTest :
    FunSpec({
        extensions(listOf(MQListener))

        val producer: JmsProducerService by lazy { JmsProducerService(connectionFactory = connectionFactory, timeout = 3000) }
        val samhandlerClientService: SamhandlerClientService by lazy {
            SamhandlerClientService(
                jmsProducerService = producer,
                tssQueue = senderQueue,
            )
        }
        val tssId = "80000415406"

        test("skal SamhandlerClientService hente samhandler fra TSS") {
            MQListener.setupMQReply(senderQueue, "samhandler/tssSamhandlerData_response.xml".readFromResource())

            val response = samhandlerClientService.getSamhandler(tssId)
            response shouldBe "VOSS HERAD"
        }

        test("skal SamhandlerClientService kaster SamhanderException når ingen navn ble funnet") {
            MQListener.setupMQReply(senderQueue, "samhandler/tssSamhandlerData_nodata_response.xml".readFromResource())

            val exception = shouldThrow<SamhanderException> { samhandlerClientService.getSamhandler(tssId) }
            exception.message shouldBe "Samhandler med tssId: 80000415406 ikke funnet"
        }

        test("skal SamhandlerClientService kaster SamhanderException når klient feilet") {
            val exception = shouldThrow<SamhanderException> { samhandlerClientService.getSamhandler(tssId) }
            exception.message shouldBe "Feil ved henting av samhandler fra TSS"
        }
    })
