package no.nav.sokos.oppdrag.venteregister.service

import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.venteregister.dto.AnsatteDTO

private val logger = KotlinLogging.logger {}
private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class VenteregisterService() {
    fun getAnsatte(): List<AnsatteDTO> {
        return listOf(
            AnsatteDTO(
                "Ola Nordmann",
                "Snekker",
            ),
            AnsatteDTO(
                "Kari Nordmann",
                "Tømrer",
            ),
            AnsatteDTO(
                "Per Hansen",
                "Elektriker",
            ),
        )
    }
}
