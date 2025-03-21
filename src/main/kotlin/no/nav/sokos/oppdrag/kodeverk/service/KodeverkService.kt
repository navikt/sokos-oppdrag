package no.nav.sokos.oppdrag.kodeverk.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.kodeverk.domain.FagGruppe
import no.nav.sokos.oppdrag.kodeverk.domain.FagOmraade
import no.nav.sokos.oppdrag.kodeverk.repository.KodeverkRepository

private val logger = KotlinLogging.logger {}

// TODO: Redis cache??

class KodeverkService(
    private val kodeverkRepository: KodeverkRepository = KodeverkRepository(),
) {
    fun getFagGrupper(): List<FagGruppe> {
        logger.info { "Henter faggrupper" }
        return kodeverkRepository.getFagGrupper()
    }

    fun getFagOmraader(): List<FagOmraade> {
        logger.info { "Henter fagområder" }
        return kodeverkRepository.getFagOmraader()
    }
}
