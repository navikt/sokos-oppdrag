package no.nav.sokos.oppdrag.fastedata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Faggruppe(
    val kodeFaggruppe: String,
    val navnFaggruppe: String,
    val skatteprosent: Int,
    // Oppgjorsordning char(2) not null
    val ventedager: Int,
    val klassekodeFeil: String,
    val klassekodeJustering: String,
    val klassekodeMotpFeil: String,
    val klassekodeMotpTrekk: String,
    val klassekodeMotpInnkr: String,
    val destinasjon: String,
    val reskontroOppdrag: String,
    val onlineBeregning: Boolean,
    val pensjon: Boolean,
    val oereavrunding: Boolean,
    val samordnetBeregning: String,
    val prioritet: Int,
    // antMndTilbake smallint not null
    // brukerId char(8) not null
    // tidspktReg timestamp not null
    //
    // Nullable:
    // AUTO_TIL_RESKONTRO     CHAR(1),
    // KODE_VALG_SKATT        CHAR(4),
    // SKATT_TIDL_AAR         CHAR(4),
    // SPLITT_PERIODE         CHAR(1),
    // REDUSER_KRAVGRUNNLAG   CHAR(1),
    // ANT_MND_OMPOSTER       SMALLINT
    //
    // Eget felt utledet fra T_FAGOMRAADER:
    val antallFagomraader: Int,
)
