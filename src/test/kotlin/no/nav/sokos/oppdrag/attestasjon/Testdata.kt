package no.nav.sokos.oppdrag.attestasjon

import no.nav.sokos.oppdrag.TestUtil.readFromResource
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonLinje
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestertStatus.ALLE
import no.nav.sokos.oppdrag.attestasjon.api.model.OppdragsRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.toDTO
import no.nav.sokos.oppdrag.common.NavIdent

const val GJELDER_ID = "24029428499"

const val INTEGRATION_BASE_API_PATH = "/api/v1/integration"
const val KODERVERK_BASE_API_PATH = "/api/v1/kodeverk"
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
            navnFaggruppe = "Arbeidsytelser",
            navnFagomraade = "Arbeidsavklaringspenger",
            fagSystemId = "123456789",
            oppdragGjelderId = GJELDER_ID,
            kodeFaggruppe = "ARBYT",
            kodeFagomraade = "AAP",
            kostnadssted = "8020",
            ansvarssted = "4819",
            oppdragsId = 58308587,
            attestanter = mutableMapOf(1 to listOf(navIdent.ident)),
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

    val attestasjonRequestTestdata =
        AttestasjonRequest(
            GJELDER_ID,
            "123456789",
            "AAP",
            58308587,
            listOf(AttestasjonLinje(1, navIdent.ident, "2021-01-01")),
        )
}
