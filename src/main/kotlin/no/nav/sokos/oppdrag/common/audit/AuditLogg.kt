package no.nav.sokos.oppdrag.common.audit

private const val VERSION = "0"
private const val DEVICE_VENDOR = "Utbetalingsportalen"
private const val DEVICE_PRODUCT = "sokos-oppdrag"
private const val DEVICE_VERSION = "1.0"
private const val DEVICE_EVENT_CLASS_ID = "audit:access"
private const val NAME = "sokos-oppdrag"
private const val SEVERITY = "INFO"

data class AuditLogg(
    val navIdent: String,
    val gjelderId: String,
    val brukerBehandlingTekst: String,
) {
    fun logMessage(): String {
        val extension = "suid=$navIdent duid=$gjelderId end=${System.currentTimeMillis()} msg=$brukerBehandlingTekst"

        return "CEF:$VERSION|$DEVICE_VENDOR|$DEVICE_PRODUCT|$DEVICE_VERSION|$DEVICE_EVENT_CLASS_ID|$NAME|$SEVERITY|$extension"
    }
}
