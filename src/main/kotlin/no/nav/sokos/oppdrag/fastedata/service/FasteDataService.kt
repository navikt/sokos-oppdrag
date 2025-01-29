package no.nav.sokos.oppdrag.fastedata.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier
import no.nav.sokos.oppdrag.fastedata.repository.FagomraadeRepository
import no.nav.sokos.oppdrag.fastedata.repository.VentekriterierRepository

private val logger = KotlinLogging.logger {}

class FasteDataService(
    private val fagomraadeRepository: FagomraadeRepository = FagomraadeRepository(),
    private val ventekriterierRepository: VentekriterierRepository = VentekriterierRepository(),
) {
    fun getFagomraader(): List<Fagomraade> {
        logger.info { "Henter fagområder" }
        return fagomraadeRepository.getFagOmraader()
    }

    fun getKorrigeringsaarsak(kodeFagomraade: String): List<Korrigeringsaarsak> {
        logger.info { "Henter korrigeringsårsaker for fagområde" }
        return fagomraadeRepository.getKorrigeringsaarsak(kodeFagomraade)
    }

    fun getAllVentekriterier(): List<Ventekriterier> {
        logger.info { "Henter alle ventekriterier" }
        return ventekriterierRepository.getAllVentekriterier()
    }
}
