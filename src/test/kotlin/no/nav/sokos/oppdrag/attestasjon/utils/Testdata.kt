package no.nav.sokos.oppdrag.attestasjon.utils

import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.ALLE
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO

const val GJELDER_ID = "12345678900"
const val KODE_FAGOMRAADE = "MOSALLE"
const val KODE_FAGGRUPPE = "ERSEPT"

object Testdata {
    val oppdragMockdata =
        Oppdrag(
            antallAttestanter = 1,
            fagGruppe = "fagGruppe",
            fagOmraade = "fagOmraade",
            fagSystemId = "fagSystemId",
            gjelderId = GJELDER_ID,
            kodeFagGruppe = KODE_FAGGRUPPE,
            kodeFagOmraade = KODE_FAGOMRAADE,
            kostnadsSted = "kostnadsSted",
            ansvarsSted = "ansvarsSted",
            oppdragsId = 1,
            attestanter = mutableMapOf(1 to listOf("attestant1")),
        )

    val oppdragDTOMockdata = oppdragMockdata.toDTO(hasWriteAccess = true)

    val oppdragRequestMockdata =
        OppdragsRequest(
            gjelderId = GJELDER_ID,
            fagSystemId = "fagSystemId",
            kodeFagGruppe = KODE_FAGGRUPPE,
            kodeFagOmraade = KODE_FAGOMRAADE,
            attestertStatus = ALLE,
        )
}
