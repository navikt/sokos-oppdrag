package no.nav.sokos.oppdrag.fastedata.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.fastedata.domain.Bilagstype
import no.nav.sokos.oppdrag.fastedata.domain.Fagomraade
import no.nav.sokos.oppdrag.fastedata.domain.Klassekode
import no.nav.sokos.oppdrag.fastedata.dto.KorrigeringsaarsakDTO
import no.nav.sokos.oppdrag.fastedata.repository.FagomraadeRepository
import no.nav.sokos.oppdrag.fastedata.validator.validateFagomraadeQueryParameter

private val logger = KotlinLogging.logger {}

class FasteDataService(
    private val fagomraadeRepository: FagomraadeRepository = FagomraadeRepository(),
) {
    fun getFagomraader(): List<Fagomraade> {
        logger.info { "Henter fagområder" }
        return fagomraadeRepository.getFagOmraader()
    }

    fun getKorrigeringsaarsaker(kodeFagomraade: String): List<KorrigeringsaarsakDTO> {
        logger.info { "Henter korrigeringsårsaker for fagområde" }
        validateFagomraadeQueryParameter(kodeFagomraade)
        return fagomraadeRepository.getKorrigeringsaarsaker(
            kodeFagomraade,
        ).map { KorrigeringsaarsakDTO(it) }
    }

    fun getBilagstyper(kodeFagomraade: String): List<Bilagstype> {
        logger.info { "Henter bilagstyper for fagområde" }
        validateFagomraadeQueryParameter(kodeFagomraade)
        return fagomraadeRepository.getBilagstyper(kodeFagomraade)
    }

    fun getKlassekoder(kodeFagomraade: String): List<Klassekode> {
        logger.info { "Henter klassekoder for fagområde" }
        validateFagomraadeQueryParameter(kodeFagomraade)
        return fagomraadeRepository.getKlassekoder(kodeFagomraade)
    }
}
