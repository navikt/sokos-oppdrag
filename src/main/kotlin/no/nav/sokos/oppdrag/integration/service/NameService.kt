package no.nav.sokos.oppdrag.integration.service

import kotlinx.serialization.Serializable

import mu.KotlinLogging

import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.client.ereg.EregClientService
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.tp.TpClientService

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class NameService(
    private val pdlClientService: PdlClientService = PdlClientService(),
    private val tpClientService: TpClientService = TpClientService(),
    private val eregClientService: EregClientService = EregClientService(),
    private val auditLogger: AuditLogger = AuditLogger(),
) {
    suspend fun getNavn(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): NameResponse {
        secureLogger.info { "Henter navn for gjelderId: $gjelderId" }
        auditLogger.auditLog(
            AuditLogg(
                navIdent = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på gjelderId for å hente navn",
            ),
        )

        return when {
            gjelderId.toLong() > 80_000_000_000 -> getLeverandorName(gjelderId)
            gjelderId.toLong() in 1_000_000_001..79_999_999_999 -> getPersonName(gjelderId)
            else -> getOrganisasjonsName(gjelderId.replace("^(00)?".toRegex(), ""))
        }
    }

    private suspend fun getLeverandorName(gjelderId: String): NameResponse {
        val leverandorName = tpClientService.getLeverandorNavn(gjelderId)
        return NameResponse(leverandorName)
    }

    private suspend fun getPersonName(gjelderId: String): NameResponse {
        val person = pdlClientService.getPerson(listOf(gjelderId))[gjelderId]?.navn?.first()
        val personName =
            person?.let {
                when (person.mellomnavn) {
                    null -> "${person.fornavn} ${person.etternavn}"
                    else -> "${person.fornavn} ${person.mellomnavn} ${person.etternavn}"
                }
            }
        return NameResponse(personName)
    }

    private suspend fun getOrganisasjonsName(gjelderId: String): NameResponse {
        val organisasjonName = eregClientService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        return NameResponse(organisasjonName)
    }
}

@Serializable
data class NameResponse(
    val navn: String?,
)
