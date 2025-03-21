package no.nav.sokos.oppdrag.kodeverk.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks

import no.nav.sokos.oppdrag.listener.Db2Listener
import no.nav.sokos.oppdrag.listener.Db2Listener.kodeverkRepository

internal class KodeverkServiceTest :
    FunSpec({
        extensions(Db2Listener)

        val kodeverkService =
            KodeverkService(
                kodeverkRepository = kodeverkRepository,
            )

        afterEach {
            clearAllMocks()
        }

        test("getFagGrupper skal returnere navn og type") {
            val result = kodeverkService.getFagGrupper()
            result.forEach { fagGruppe ->
                fagGruppe.navn shouldNotBe null
                fagGruppe.type shouldNotBe null
            }
        }

        test("getFagOmraader skal returnere en liste med FagOmraade") {
            val fagomraader = kodeverkService.getFagOmraader()
            fagomraader.size shouldBe 293
            fagomraader.forEach {
                it.kodeFagomraade shouldNotBe null
                it.kodeFagomraade shouldNotBe null
            }
        }
    })
