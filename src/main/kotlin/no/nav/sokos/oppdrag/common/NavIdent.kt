package no.nav.sokos.oppdrag.common

const val GRUPPE_FORTROLIG = "0000-GA-okonomi-fortrolig"
const val GRUPPE_STRENGT_FORTROLIG = "0000-GA-okonomi-strengt_fortrolig"
const val GRUPPE_EGNE_ANSATTE = "0000-GA-okonomi-egne_ansatte"

const val GRUPPE_ATTESTASJON_NOS_READ = "0000-GA-SOKOS-MF-Attestasjon-NOS-READ"
const val GRUPPE_ATTESTASJON_NOS_WRITE = "0000-GA-SOKOS-MF-Attestasjon-NOS-WRITE"
const val GRUPPE_ATTESTASJON_NOP_READ = "0000-GA-SOKOS-MF-Attestasjon-NOP-READ"
const val GRUPPE_ATTESTASJON_NOP_WRITE = "0000-GA-SOKOS-MF-Attestasjon-NOP-WRITE"
const val GRUPPE_ATTESTASJON_NASJONALT_READ = "0000-GA-SOKOS-MF-Attestasjon-Nasjonalt-READ"
const val GRUPPE_ATTESTASJON_NASJONALT_WRITE = "0000-GA-SOKOS-MF-Attestasjon-Nasjonalt-WRITE"

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun hasAccessFortrolig(): Boolean = roller.contains(GRUPPE_FORTROLIG)

    fun hasAccessStrengtFortrolig(): Boolean = roller.contains(GRUPPE_STRENGT_FORTROLIG)

    fun hasAccessEgneAnsatte(): Boolean = roller.contains(GRUPPE_EGNE_ANSATTE)

    fun hasReadAccessNOS(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOS_READ)

    fun hasWriteAccessNOS(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOS_WRITE)

    fun hasReadAccessNOP(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOP_READ)

    fun hasWriteAccessNOP(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NOP_WRITE)

    fun hasReadAccessLandsdekkende(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NASJONALT_READ)

    fun hasWriteAccessLandsdekkende(): Boolean = roller.contains(GRUPPE_ATTESTASJON_NASJONALT_WRITE)

    fun hasWriteAccessAttestasjon(): Boolean = hasWriteAccessLandsdekkende() || hasWriteAccessNOS() || hasWriteAccessNOP()
}
