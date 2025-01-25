package no.nav.sokos.oppdrag.attestasjon

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.ALLE
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO
import no.nav.sokos.oppdrag.common.NavIdent

const val GJELDER_ID = "24029428499"

const val APPLICATION_JSON = "application/json"
const val INTEGRATION_BASE_API_PATH = "/api/v1/integration"
const val OPPDRAGSINFO_BASE_API_PATH = "/api/v1/oppdragsinfo"
const val ATTESTASJON_BASE_API_PATH = "/api/v1/attestasjon"
const val FASTEDATA_BASE_API_PATH = "/api/v1/fastedata"

object Testdata {
    val navIdent = NavIdent("Z999999")
    val tokenWithNavIdent = "tokenWithNavIdent.txt".readFromResource()
    val tokenWithoutNavIdent = "tokenWithoutNavIdent.txt".readFromResource()

    private val oppdragTestdata =
        Oppdrag(
            antAttestanter = 1,
            navnFaggruppe = "fagGruppe",
            navnFagomraade = "fagOmraade",
            fagSystemId = "fagSystemId",
            oppdragGjelderId = GJELDER_ID,
            kodeFaggruppe = "ERSEPT",
            kodeFagomraade = "MOSALLE",
            kostnadssted = "kostnadsSted",
            ansvarssted = "ansvarsSted",
            oppdragsId = 1,
            attestanter = mutableMapOf(1 to listOf("attestant1")),
        )

    val oppdragDTOTestdata = oppdragTestdata.toDTO(hasWriteAccess = true)

    val oppdragRequestTestdata =
        OppdragsRequest(
            gjelderId = GJELDER_ID,
            fagSystemId = "",
            kodeFagGruppe = null,
            kodeFagOmraade = null,
            attestertStatus = ALLE,
        )
}
