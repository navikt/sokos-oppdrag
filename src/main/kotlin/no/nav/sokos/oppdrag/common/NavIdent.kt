package no.nav.sokos.oppdrag.common

const val GRUPPE_FORTROLIG = "0000-GA-okonomi-fortrolig"
const val GRUPPE_STRENGT_FORTROLIG = "0000-GA-okonomi-strengt_fortrolig"
const val GRUPPE_EGNE_ANSATTE = "0000-GA-okonomi-egne_ansatte"

const val GRUPPE_ATTESTASJON_NOS_READ = "0000-GA-SOKOS-MF-Attestasjon-NOS-READ"
const val GRUPPE_ATTESTASJON_NOS_WRITE = "0000-GA-SOKOS-MF-Attestasjon-NOS-WRITE"
const val GRUPPE_ATTESTASJON_NOP_READ = "0000-GA-SOKOS-MF-Attestasjon-NOP-READ"
const val GRUPPE_ATTESTASJON_NOP_WRITE = "0000-GA-SOKOS-MF-Attestasjon-NOP-WRITE"
const val GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ = "0000-GA-SOKOS-MF-Attestasjon-Landsdekkende-READ"
const val GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE = "0000-GA-SOKOS-MF-Attestasjon-Landsdekkende-WRITE"

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun hasAccessFortrolig(): Boolean {
        return roller.contains(GRUPPE_FORTROLIG)
    }

    fun hasAccessStrengtFortrolig(): Boolean {
        return roller.contains(GRUPPE_STRENGT_FORTROLIG)
    }

    fun hasAccessEgneAnsatte(): Boolean {
        return roller.contains(GRUPPE_EGNE_ANSATTE)
    }

    fun hasReadAccessNOS(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_NOS_READ)
    }

    fun hasWriteAccessNOS(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_NOS_WRITE)
    }

    fun hasReadAccessNOP(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_NOP_READ)
    }

    fun hasWriteAccessNOP(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_NOP_WRITE)
    }

    fun hasReadAccessLandsdekkende(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_LANDSDEKKENDE_READ)
    }

    fun hasWriteAccessLandsdekkende(): Boolean {
        return roller.contains(GRUPPE_ATTESTASJON_LANDSDEKKENDE_WRITE)
    }
}
