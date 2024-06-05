package no.nav.sokos.oppdrag.integration.service

import io.ktor.server.application.ApplicationCall
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.tp.TpService
import no.nav.sokos.oppdrag.oppdragsinfo.service.secureLogger
import no.nav.sokos.oppdrag.security.JwtClaimHandler.getSaksbehandler

private const val GJELDER_ID_VALUE_CHECK = 80000000000

class IntegrationService(
    private val pdlService: PdlService = PdlService(),
    private val tpService: TpService = TpService(),
    private val eregService: EregService = EregService(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    suspend fun hentNavnForGjelderId(
        gjelderId: String,
        applicationCall: ApplicationCall,
    ): String {
        val saksbehandler = getSaksbehandler(applicationCall)

        secureLogger.info { "Henter navn for gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
            ),
        )

        return when {
            gjelderId.toLong() > GJELDER_ID_VALUE_CHECK -> tpService.getLeverandorNavn(gjelderId).navn
            gjelderId.toLong() < GJELDER_ID_VALUE_CHECK ->
                pdlService.getPersonNavn(gjelderId)?.navn?.first()?.let { navn ->
                    navn.mellomnavn?.let { "${navn.fornavn} ${navn.mellomnavn} ${navn.etternavn}" } ?: "${navn.fornavn} ${navn.etternavn}"
                } ?: ""

            else -> eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        }
    }
}
