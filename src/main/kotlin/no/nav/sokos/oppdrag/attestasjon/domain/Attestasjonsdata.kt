package no.nav.sokos.oppdrag.attestasjon.domain

import kotlinx.serialization.Serializable

@Serializable
data class Attestasjonsdata(
    val kode_faggruppe: String,
    val navn_faggruppe: String,
    val kode_fagomraade: String,
    val navn_fagomraade: String,
    val oppdrags_id: Int,
    val fagsystem_id: String,
    val oppdrag_gjelder_id: String,
    val ant_attestanter: Int,
    val linje_id: Int,
    val attestert: String,
    val dato_vedtak_fom: String,
    val dato_vedtak_tom: String?,
    val kode_status: String,
)
