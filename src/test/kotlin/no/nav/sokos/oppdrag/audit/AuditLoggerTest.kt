package no.nav.sokos.oppdrag.audit

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import no.nav.sokos.oppdrag.common.audit.AuditLogg

internal class AuditLoggerTest : FunSpec({

    test("test auditLogger har riktig melding format") {
        val expectedLogMessageStart =
            "CEF:0|Utbetalingsportalen|sokos-oppdrag|1.0|audit:access|sokos-oppdrag|INFO|suid=Z12345 duid=24417337179 end="
        val expectedLogMessageEnd = " msg=NAV-ansatt har gjort et søk på oppdrag"
        val logData =
            AuditLogg(
                saksbehandler = "Z12345",
                gjelderId = "24417337179",
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på oppdrag",
            )

        logData.logMessage() shouldStartWith expectedLogMessageStart
        logData.logMessage() shouldEndWith expectedLogMessageEnd
    }
})
