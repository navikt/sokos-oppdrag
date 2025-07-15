package no.nav.sokos.oppdrag.kodeverk.service

import mu.KotlinLogging

import no.nav.sokos.oppdrag.common.valkey.ValkeyCache
import no.nav.sokos.oppdrag.config.ValkeyConfig.createCodec
import no.nav.sokos.oppdrag.kodeverk.domain.FagGruppe
import no.nav.sokos.oppdrag.kodeverk.domain.FagOmraade
import no.nav.sokos.oppdrag.kodeverk.repository.KodeverkRepository

private val logger = KotlinLogging.logger {}

class KodeverkService(
    private val kodeverkRepository: KodeverkRepository = KodeverkRepository(),
    private val valkeyCache: ValkeyCache = ValkeyCache(name = "kodeverkServcie"),
) {
    suspend fun getFagGrupper(): List<FagGruppe> =
        valkeyCache.getAsync(key = "faggrupper", codec = createCodec<List<FagGruppe>>("get-faggrupper")) {
            kodeverkRepository.getFagGrupper()
        }

    suspend fun getFagOmraader(): List<FagOmraade> =
        valkeyCache.getAsync(key = "fagomraader", codec = createCodec<List<FagOmraade>>("get-fagomraader")) {
            kodeverkRepository.getFagOmraader()
        }
}
