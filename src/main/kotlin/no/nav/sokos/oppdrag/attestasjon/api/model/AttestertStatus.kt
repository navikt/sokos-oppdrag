package no.nav.sokos.oppdrag.attestasjon.api.model

enum class AttestertStatus(
    val attestert: Boolean?,
    val filterEgenAttestert: Boolean?,
) {
    IKKE_FERDIG_ATTESTERT_EKSL_EGNE(false, false),
    IKKE_FERDIG_ATTESTERT_INKL_EGNE(false, null),
    ATTESTERT(true, null),
    ALLE(null, null),
    EGEN_ATTESTERTE(true, true),
}
