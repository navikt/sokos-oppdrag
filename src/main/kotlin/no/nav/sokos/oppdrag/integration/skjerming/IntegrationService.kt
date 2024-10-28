package no.nav.sokos.oppdrag.integration.skjerming

import mu.KotlinLogging
import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.NavIdent
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdResponse
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.tp.TpService

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class IntegrationService(
    private val pdlClientService: PdlClientService = PdlClientService(),
    private val tpService: TpService = TpService(),
    private val eregService: EregService = EregService(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val skjermetClientService: SkjermetClientService = SkjermetClientService(),
) {
    suspend fun getNavnForGjelderId(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): GjelderIdResponse {
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
            gjelderId.toLong() in 1_000_000_001..79_999_999_999 -> getPersonName(gjelderId, saksbehandler)
            else -> getOrganisasjonsName(gjelderId.replace("^(00)?".toRegex(), ""))
        }
    }

    suspend fun checkSkjermetPerson(
        gjelderId: String,
        saksbehandler: NavIdent,
    ) {
        if (gjelderId.toLong() !in 1_000_000_001..79_999_999_999) return

        val graderinger: List<AdressebeskyttelseGradering?> =
            pdlClientService.getPerson(listOf(gjelderId))[gjelderId]?.adressebeskyttelse?.map { it.gradering } ?: emptyList()

        val isPersonSkjermet =
            when {
                !saksbehandler.harTilgangTilFortrolig() && graderinger.contains(AdressebeskyttelseGradering.FORTROLIG) -> true
                !saksbehandler.harTilgangTilStrengtFortrolig() && graderinger.contains(AdressebeskyttelseGradering.STRENGT_FORTROLIG) -> true
                !saksbehandler.harTilgangTilEgneAnsatte() && skjermetClientService.isSkjermedePersonerInSkjermingslosningen(listOf(gjelderId))[gjelderId] == true -> true

                else -> false
            }
        if (isPersonSkjermet) {
            throw SkjermetException("Du har ikke tilgang til å se denne personen")
        }
    }

    private suspend fun getLeverandorName(gjelderId: String): GjelderIdResponse {
        val leverandorName = tpService.getLeverandorNavn(gjelderId).navn
        return GjelderIdResponse(leverandorName)
    }

    private suspend fun getPersonName(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): GjelderIdResponse {
        checkSkjermetPerson(gjelderId, saksbehandler)
        val person = pdlClientService.getPerson(listOf(gjelderId))[gjelderId]?.navn?.first()
        val personName =
            person?.let {
                when (person.mellomnavn) {
                    null -> "${person.fornavn} ${person.etternavn}"
                    else -> "${person.fornavn} ${person.mellomnavn} ${person.etternavn}"
                }
            }
        return GjelderIdResponse(personName)
    }

    private suspend fun getOrganisasjonsName(gjelderId: String): GjelderIdResponse {
        val organisasjonName = eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        return GjelderIdResponse(organisasjonName)
    }
}

data class SkjermetException(override val message: String) : Exception(message)
