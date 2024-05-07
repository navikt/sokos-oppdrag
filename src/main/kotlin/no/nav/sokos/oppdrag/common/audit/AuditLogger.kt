package no.nav.sokos.oppdrag.common.audit

import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.config.AUDIT_LOGGER

private val auditLogger = KotlinLogging.logger(AUDIT_LOGGER)

class AuditLogger {
    fun auditLog(auditLoggData: AuditLogg) {
        auditLogger.info(auditLoggData.logMessage())
    }
}
