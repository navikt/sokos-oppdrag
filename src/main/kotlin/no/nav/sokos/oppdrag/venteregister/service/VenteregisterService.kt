package no.nav.sokos.oppdrag.venteregister.service

import no.nav.sokos.oppdrag.venteregister.dto.AnsatteDTO

class VenteregisterService {
    fun getAnsatte(): List<AnsatteDTO> =
        listOf(
            AnsatteDTO(
                1,
                "Ola Nordmann",
                "Snekker",
            ),
            AnsatteDTO(
                2,
                "Kari Nordmann",
                "Tømrer",
            ),
            AnsatteDTO(
                3,
                "Per Hansen",
                "Elektriker",
            ),
        )
}
