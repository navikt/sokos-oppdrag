package no.nav.sokos.oppdrag.attestasjon.domain

import no.nav.sokos.oppdrag.attestasjon.dto.OppdragDTO

data class Oppdrag(
    val ansvarsSted: String? = null,
    val antallAttestanter: Int,
    val fagGruppe: String,
    val fagOmraade: String,
    val fagSystemId: String,
    val gjelderId: String,
    val kodeFagGruppe: String,
    val kodeFagOmraade: String,
    val kostnadsSted: String,
    val oppdragsId: Int,
    val attestanter: List<String>,
)

fun Oppdrag.toDTO(
    erSkjermetForSaksbehandler: Boolean = false,
    hasWriteAccess: Boolean = false,
) = OppdragDTO(
    ansvarsSted = this.ansvarsSted,
    antallAttestanter = this.antallAttestanter,
    fagGruppe = this.fagGruppe,
    fagOmraade = this.fagOmraade,
    fagSystemId = this.fagSystemId,
    gjelderId = this.gjelderId,
    kodeFagGruppe = this.kodeFagGruppe,
    kodeFagOmraade = this.kodeFagOmraade,
    kostnadsSted = this.kostnadsSted,
    oppdragsId = this.oppdragsId,
    erSkjermetForSaksbehandler = erSkjermetForSaksbehandler,
    hasWriteAccess = hasWriteAccess,
)
