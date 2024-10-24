package no.nav.sokos.oppdrag.integration.service

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.pdl.hentperson.Person
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.NavIdent
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdResponse
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetClientImpl
import no.nav.sokos.oppdrag.integration.skjerming.SkjermetService
import no.nav.sokos.oppdrag.integration.tp.TpService

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class IntegrationService(
    private val pdlService: PdlService = PdlService(),
    private val tpService: TpService = TpService(),
    private val eregService: EregService = EregService(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val skjermetService: SkjermetService = SkjermetService(SkjermetClientImpl()),
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

    suspend fun sjekkSkjerming(gjelderId: String?, saksbehandler: NavIdent) {
        if (gjelderId?.toLong() !in 1_000_000_001..79_999_999_999) return

        val graderinger: List<AdressebeskyttelseGradering?> =
            getPerson(gjelderId!!)?.adressebeskyttelse?.map { it?.gradering } ?: emptyList()

        if (!saksbehandler.harTilgangTilStrengtFortrolig() && graderinger.contains(AdressebeskyttelseGradering.STRENGT_FORTROLIG)) {
            throw SkjermetException("Skjermet person: (strengt fortrolig)")
        }
        if (!saksbehandler.harTilgangTilFortrolig() && graderinger.contains(AdressebeskyttelseGradering.FORTROLIG)) {
            throw SkjermetException("Skjermet person (fortrolig)")
        }
        if (!skjermetService.kanSaksbehandlerSePerson(gjelderId, saksbehandler)) {
            throw SkjermetException("Skjermet person (egen ansatt)")
        }

    }

    private suspend fun getPerson(gjelderId: String): Person? {
        val person = pdlService.getPersonNavn(gjelderId)
        return person
    }

    private suspend fun getLeverandorName(gjelderId: String): GjelderIdResponse {
        val leverandorName = tpService.getLeverandorNavn(gjelderId).navn
        return GjelderIdResponse(leverandorName)
    }

    private suspend fun getPersonName(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): GjelderIdResponse {
        runBlocking { sjekkSkjerming(gjelderId, saksbehandler) }
        val person = pdlService.getPersonNavn(gjelderId)?.navn?.first()
        val personName = person?.mellomnavn?.let { "${person.fornavn} ${person.mellomnavn} ${person.etternavn}" }
            ?: "${person?.fornavn} ${person?.etternavn}"
        return GjelderIdResponse(personName)
    }

    private suspend fun getOrganisasjonsName(gjelderId: String): GjelderIdResponse {
        val organisasjonName = eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        return GjelderIdResponse(organisasjonName)
    }
}

class SkjermetException(val msg: String) : Exception()
