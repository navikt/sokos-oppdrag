package no.nav.sokos.oppdrag.integration.service

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.util.getAsync
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.skjerming.SkjermetClientService
import java.time.Duration

class SkjermingService(
    private val pdlClientService: PdlClientService = PdlClientService(),
    private val skjermetClientService: SkjermetClientService = SkjermetClientService(),
) {
    private val bolkPdlCache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .buildAsync<String, Map<String, Person>>()

    private val bolkEgneAnsatteCache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .buildAsync<String, Map<String, Boolean>>()

    suspend fun getSkjermingForIdentListe(
        identer: List<String>,
        saksbehandler: NavIdent,
    ): Map<String, Boolean> {
        val deduplicatedIdenter = identer.distinct()
        val personIdenter = deduplicatedIdenter.filter { it.toLong() in 1_000_000_001..79_999_999_999 }

        if (personIdenter.isEmpty()) {
            return deduplicatedIdenter.associateWith { false }
        }

        val egenAnsattMap =
            bolkEgneAnsatteCache
                .getAsync(personIdenter.joinToString()) {
                    skjermetClientService.isSkjermedePersonerInSkjermingslosningen(personIdenter)
                }.mapValues { (_, skjermet) -> !saksbehandler.harTilgangTilEgneAnsatte() && skjermet }

        val adressebeskyttelseMap =
            bolkPdlCache
                .getAsync(personIdenter.joinToString()) {
                    pdlClientService.getPerson(identer = personIdenter)
                }.mapValues { (_, person) ->
                    val graderinger = person.adressebeskyttelse.map { it.gradering }
                    !saksbehandler.harTilgangTilFortrolig() &&
                        AdressebeskyttelseGradering.FORTROLIG in graderinger ||
                        !saksbehandler.harTilgangTilStrengtFortrolig() &&
                        graderinger.any {
                            it == AdressebeskyttelseGradering.STRENGT_FORTROLIG || it == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
                        }
                }

        return deduplicatedIdenter.associateWith { key ->
            egenAnsattMap[key] == true || adressebeskyttelseMap[key] == true
        }
    }

    suspend fun getSkjermingForIdent(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): Boolean = getSkjermingForIdentListe(listOf(gjelderId), saksbehandler)[gjelderId] ?: false
}
