package no.nav.sokos.oppdrag.audit

data class AuditLogg(
    val saksbehandler: String,
    val gjelderId: String,
    val brukerBehandlingTekst: String,
) {
    private val version = "0"
    private val deviceVendor = "Utbetalingsportalen"
    private val deviceProduct = "sokos-oppdrag"
    private val deviceVersion = "1.0"
    private val deviceEventClassId = "audit:access"
    private val name = "sokos-oppdrag"
    private val severity = "INFO"
    private val brukerhandling = brukerBehandlingTekst

    fun logMessage(): String {
        val extension = "suid=$saksbehandler duid=$gjelderId end=${System.currentTimeMillis()} msg=$brukerhandling"

        return "CEF:$version|$deviceVendor|$deviceProduct|$deviceVersion|$deviceEventClassId|$name|$severity|$extension"
    }
}
