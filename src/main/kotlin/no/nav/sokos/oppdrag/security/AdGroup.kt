package no.nav.sokos.oppdrag.security

enum class AdGroup(
    val adGroupName: String,
) {
    FORTROLIG("0000-GA-okonomi-fortrolig"),
    STRENGT_FORTROLIG("0000-GA-okonomi-strengt_fortrolig"),
    EGNE_ANSATTE("0000-GA-okonomi-egne_ansatte"),

    ATTESTASJON_NOS_READ("0000-CA-SOKOS-MF-Attestasjon-NOS-READ"),
    ATTESTASJON_NOS_WRITE("0000-CA-SOKOS-MF-Attestasjon-NOS-WRITE"),
    ATTESTASJON_NOP_READ("0000-CA-SOKOS-MF-Attestasjon-NOP-READ"),
    ATTESTASJON_NOP_WRITE("0000-CA-SOKOS-MF-Attestasjon-NOP-WRITE"),
    ATTESTASJON_NASJONALT_READ("0000-CA-SOKOS-MF-Attestasjon-Nasjonalt-READ"),
    ATTESTASJON_NASJONALT_WRITE("0000-CA-SOKOS-MF-Attestasjon-Nasjonalt-WRITE"),

    OPPDRAGSINFO_NASJONALT_READ("0000-CA-SOKOS-MF-Oppdragsinfo-Nasjonalt-READ"),
    OPPDRAGSINFO_NOP_READ("0000-CA-SOKOS-MF-Oppdragsinfo-NOP-READ"),
    OPPDRAGSINFO_NOS_READ("0000-CA-SOKOS-MF-Oppdragsinfo-NOS-READ"),
}
