package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

import no.nav.sokos.oppdrag.attestasjon.dto.OppdragDTO

@Serializable
data class Oppdrag(
    val ansvarssted: String? = null,
    val antAttestanter: Int,
    val navnFaggruppe: String,
    val navnFagomraade: String,
    val fagSystemId: String,
    val oppdragGjelderId: String,
    val kodeFaggruppe: String,
    val kodeFagomraade: String,
    val kostnadssted: String,
    val oppdragsId: Int,
    val attestanter: MutableMap<Int, List<String>> = mutableMapOf(),
)

fun Oppdrag.toDTO(
    erSkjermetForSaksbehandler: Boolean = false,
    hasWriteAccess: Boolean = false,
) = OppdragDTO(
    ansvarssted = this.ansvarssted,
    antAttestanter = this.antAttestanter,
    navnFaggruppe = this.navnFaggruppe,
    navnFagomraade = this.navnFagomraade,
    fagSystemId = this.fagSystemId,
    oppdragGjelderId = this.oppdragGjelderId,
    kodeFaggruppe = this.kodeFaggruppe,
    kodeFagomraade = this.kodeFagomraade,
    kostnadssted = this.kostnadssted,
    oppdragsId = this.oppdragsId,
    erSkjermetForSaksbehandler = erSkjermetForSaksbehandler,
    hasWriteAccess = hasWriteAccess,
)
