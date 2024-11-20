package no.nav.sokos.oppdrag.common

const val GRUPPE_FORTROLIG = "0000-GA-okonomi-fortrolig"
const val GRUPPE_STRENGT_FORTROLIG = "0000-GA-okonomi-strengt_fortrolig"
const val GRUPPE_EGNE_ANSATTE = "0000-GA-okonomi-egne_ansatte"
const val GRUPPE_ATTESTASJON_SKRIV = "0000-GA-SOKOS-MF-Attestasjon-WRITE"
const val GRUPPE_NOS = "0000-GA-SOKOS-MF-Attestasjon-NOS-READ"
const val GRUPPE_TOS = "0000-GA-SOKOS-MF-Attestasjon-TOS-READ"
const val GRUPPE_LANDSDEKKENDE = "0000-GA-SOKOS-MF-Attestasjon-Landsdekkende-READ"

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun harTilgangTilFortrolig(): Boolean {
        return roller.contains(GRUPPE_FORTROLIG)
    }

    fun harTilgangTilStrengtFortrolig(): Boolean {
        return roller.contains(GRUPPE_STRENGT_FORTROLIG)
    }

    fun harTilgangTilEgneAnsatte(): Boolean {
        return roller.contains(GRUPPE_EGNE_ANSATTE)
    }

    fun harTilgangTilNos(): Boolean {
        return roller.contains(GRUPPE_NOS)
    }

    fun harTilgangTilNop(): Boolean {
        return roller.contains(GRUPPE_TOS)
    }

    fun harTilgangTilLandsdekkende(): Boolean {
        return roller.contains(GRUPPE_LANDSDEKKENDE)
    }
}
