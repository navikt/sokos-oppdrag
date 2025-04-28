package no.nav.sokos.oppdrag.common

const val GRUPPE_FORTROLIG = "0000-GA-okonomi-fortrolig"
const val GRUPPE_STRENGT_FORTROLIG = "0000-GA-okonomi-strengt_fortrolig"
const val GRUPPE_EGNE_ANSATTE = "0000-GA-okonomi-egne_ansatte"

const val GRUPPE_ATTESTASJON_NOS_READ = "0000-CA-SOKOS-MF-Attestasjon-NØS-READ"
const val GRUPPE_ATTESTASJON_NOS_WRITE = "0000-CA-SOKOS-MF-Attestasjon-NØS-WRITE"
const val GRUPPE_ATTESTASJON_NOP_READ = "0000-CA-SOKOS-MF-Attestasjon-NØP-READ"
const val GRUPPE_ATTESTASJON_NOP_WRITE = "0000-CA-SOKOS-MF-Attestasjon-NØP-WRITE"
const val GRUPPE_ATTESTASJON_NASJONALT_READ = "0000-CA-SOKOS-MF-Attestasjon-Nasjonalt-READ"
const val GRUPPE_ATTESTASJON_NASJONALT_WRITE = "0000-CA-SOKOS-MF-Attestasjon-Nasjonalt-WRITE"
const val GRUPPE_OPPDRAGSINFO_NASJONALT_READ = "0000-CA-SOKOS-MF-Oppdragsinfo-Nasjonalt-READ"
const val GRUPPE_OPPDRAGSINFO_NOP_READ = "0000-CA-SOKOS-MF-Oppdragsinfo-NØP-READ"
const val GRUPPE_OPPDRAGSINFO_NOS_READ = "0000-CA-SOKOS-MF-Oppdragsinfo-NØS-READ"

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun hasAccessFortrolig(): Boolean = roller.contains(GRUPPE_FORTROLIG)

    fun hasAccessStrengtFortrolig(): Boolean = roller.contains(GRUPPE_STRENGT_FORTROLIG)

    fun hasAccessEgneAnsatte(): Boolean = roller.contains(GRUPPE_EGNE_ANSATTE)

    fun hasReadAccessAttestasjonNOS(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOS_READ)

    fun hasWriteAccessAttestasjonNOS(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOS_WRITE)

    fun hasReadAccessAttestasjonNOP(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOP_READ)

    fun hasWriteAccessAttestasjonNOP(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOP_WRITE)

    fun hasReadAccessAttestasjonNasjonalt(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NASJONALT_READ)

    fun hasWriteAccessAttestasjonNasjonalt(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NASJONALT_WRITE)

    fun hasWriteAccessAttestasjon(): Boolean = hasWriteAccessAttestasjonNasjonalt() || hasWriteAccessAttestasjonNOS() || hasWriteAccessAttestasjonNOP()

    fun hasReadAccessOppdragsinfoNasjonalt(): Boolean = roller.contains(GRUPPE_OPPDRAGSINFO_NASJONALT_READ)

    fun hasReadAccessOppdragsinfoNOP(): Boolean = roller.contains(GRUPPE_OPPDRAGSINFO_NOP_READ)

    fun hasReadAccessOppdragsinfoNOS(): Boolean = roller.contains(GRUPPE_OPPDRAGSINFO_NOS_READ)
}
