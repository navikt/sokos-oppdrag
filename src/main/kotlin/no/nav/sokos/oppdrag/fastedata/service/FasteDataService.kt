package no.nav.sokos.oppdrag.fastedata.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.repository.FasteDataRepository

private val logger = KotlinLogging.logger {}

class FasteDataService(
    private val fasteDataRepository: FasteDataRepository = FasteDataRepository(),
) {
    fun getFagomraader(): List<Fagomraade> {
        logger.info { "Henter fagområder" }
        return fasteDataRepository.getFagOmraader()
    }

    fun getKorrigeringsaarsakForFagomraade(kodeFagomraade: String): List<Korrigeringsaarsak> {
        logger.info { "Henter korrigeringsårsaker for fagområde" }
        return fasteDataRepository.getKorrigeringsaarsakForFagomraade(kodeFagomraade)
    }
}
