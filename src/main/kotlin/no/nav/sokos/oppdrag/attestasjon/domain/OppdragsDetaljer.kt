@file:Suppress("ktlint")

package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class OppdragsDetaljer(
    val datoVedtakFom: String, // linje
    val delytelsesId: String, // linje
    val linjeId: String, // linje
    val oppdragsId: String, // linje
    val sats: Double, // linje
    val satstype: String, // linje
    val kodeKlasse: String, // linje
    val datoVedtakTom: String? = null, // UTLEDES
    val ansvarsStedForOppdrag: String? = null, // oppdrags_enhet
    val ansvarsStedForOppdragsLinje: String? = null, // linje_enhet
    val kostnadsStedForOppdrag: String, // oppdrags_enhet
    val kostnadsStedForOppdragsLinje: String? = null, // linje_enhet
    val antallAttestanter: Int, // attestasjon
    val attestant: String? = null, // attestasjon
    val datoUgyldigFom: String? = null, // attestasjon
    val fagGruppe: String, // faggruppe
    val fagOmraade: String, // fagomrade
    val fagSystemId: String, // oppdrag
    val gjelderId: String, // oppdrag
    val kodeFagOmraade: String, // oppdrag
)
