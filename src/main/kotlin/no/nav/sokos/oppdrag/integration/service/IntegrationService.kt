package no.nav.sokos.oppdrag.integration.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.model.GjelderIdName
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.tp.TpService
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class IntegrationService(
    private val pdlService: PdlService = PdlService(),
    private val tpService: TpService = TpService(),
    private val eregService: EregService = EregService(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    suspend fun hentNavnForGjelderId(
        gjelderId: String,
        applicationCall: ApplicationCall,
    ): GjelderIdName {
        val saksbehandler = getSaksbehandler(applicationCall)

        secureLogger.info { "Henter navn for gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på gjelderId for å hente navn",
            ),
        )

        return when {
            gjelderId.toLong() > 80000000000 -> getLeverandorName(gjelderId)
            gjelderId.toLong() in 10000000001..79999999999 -> getPersonName(gjelderId)
            else -> getOrganisasjonsName(gjelderId)
        }
    }

    private suspend fun getLeverandorName(gjelderId: String): GjelderIdName {
        val leverandorName = tpService.getLeverandorNavn(gjelderId).navn
        return GjelderIdName(leverandorName)
    }

    private suspend fun getPersonName(gjelderId: String): GjelderIdName {
        val person = pdlService.getPersonNavn(gjelderId)?.navn?.first()
        val personName = person?.mellomnavn?.let { "${person.fornavn} ${person.mellomnavn} ${person.etternavn}" } ?: "${person?.fornavn} ${person?.etternavn}"
        return GjelderIdName(personName)
    }

    private suspend fun getOrganisasjonsName(gjelderId: String): GjelderIdName {
        val organisasjonName = eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        return GjelderIdName(organisasjonName)
    }
}
