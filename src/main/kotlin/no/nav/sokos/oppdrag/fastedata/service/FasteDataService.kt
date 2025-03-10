package no.nav.sokos.oppdrag.fastedata.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Klassekode
import no.nav.sokos.oppdrag.fastedata.domain.Korrigeringsaarsak
import no.nav.sokos.oppdrag.fastedata.domain.Ventekriterier
import no.nav.sokos.oppdrag.fastedata.domain.Ventestatuskode
import no.nav.sokos.oppdrag.fastedata.repository.FagomraadeRepository
import no.nav.sokos.oppdrag.fastedata.repository.VentekriterierRepository
import no.nav.sokos.oppdrag.fastedata.repository.VentestatuskodeRepository

private val logger = KotlinLogging.logger {}

class FasteDataService(
    private val fagomraadeRepository: FagomraadeRepository = FagomraadeRepository(),
    private val ventekriterierRepository: VentekriterierRepository = VentekriterierRepository(),
    private val ventestatuskodeRepository: VentestatuskodeRepository = VentestatuskodeRepository(),
) {
    fun getAllVentestatuskoder(): List<Ventestatuskode> {
        logger.info { "Henter alle ventestatuskoder" }
        return ventestatuskodeRepository.getAllVentestatuskoder()
    }

    fun getAllVentekriterier(): List<Ventekriterier> {
        logger.info { "Henter alle ventekriterier" }
        return ventekriterierRepository.getAllVentekriterier()
    }

    fun getFagomraader(): List<Fagomraade> {
        logger.info { "Henter fagområder" }
        return fagomraadeRepository.getFagOmraader()
    }

    fun getKorrigeringsaarsaker(kodeFagomraade: String): List<Korrigeringsaarsak> {
        logger.info { "Henter korrigeringsårsaker for fagområde $kodeFagomraade" }
        return fagomraadeRepository.getKorrigeringsaarsaker(
            kodeFagomraade,
        )
    }

    fun getBilagstyper(kodeFagomraade: String): List<Bilagstype> {
        logger.info { "Henter bilagstyper for fagområde $kodeFagomraade" }
        return fagomraadeRepository.getBilagstyper(kodeFagomraade)
    }

    fun getKlassekoder(kodeFagomraade: String): List<Klassekode> {
        logger.info { "Henter klassekoder for fagområde $kodeFagomraade" }
        return fagomraadeRepository.getKlassekoder(kodeFagomraade)
    }
}
