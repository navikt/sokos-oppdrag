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
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .buildAsync<String, Map<String, Person>>()

    private val bolkEgneAnsatteCache =
        Caffeine.newBuilder()
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
            bolkEgneAnsatteCache.getAsync(personIdenter.joinToString()) { _ ->
                skjermetClientService.isSkjermedePersonerInSkjermingslosningen(
                    personIdenter,
                )
            }.map { (fnr, skjermet) -> fnr to (!saksbehandler.harTilgangTilEgneAnsatte() && skjermet) }.toMap()

        val adressebeskyttelseMap =
            bolkPdlCache.getAsync(personIdenter.joinToString()) { _ ->
                pdlClientService.getPerson(identer = personIdenter)
            }.map { (fnr, person) ->
                val graderinger = person.adressebeskyttelse.map { it.gradering }

                when {
                    !saksbehandler.harTilgangTilFortrolig() && graderinger.contains(AdressebeskyttelseGradering.FORTROLIG) -> fnr to true
                    !saksbehandler.harTilgangTilStrengtFortrolig() &&
                        graderinger.intersect(listOf(AdressebeskyttelseGradering.STRENGT_FORTROLIG, AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)).isNotEmpty() -> fnr to true

                    else -> fnr to false
                }
            }.toMap()

        val list =
            deduplicatedIdenter
                .distinct()
                .associateWith { key ->
                    val egenAnsattValue = egenAnsattMap[key] != false
                    val adressebeskyttelseValue = adressebeskyttelseMap[key] != false
                    egenAnsattValue || adressebeskyttelseValue
                }

        return list
    }

    suspend fun getSkjermingForIdent(
        gjelderId: String,
        saksbehandler: NavIdent,
    ): Boolean {
        val graderinger =
            bolkPdlCache.getAsync(gjelderId) { _ ->
                pdlClientService.getPerson(listOf(gjelderId))
            }[gjelderId]?.adressebeskyttelse?.map { it.gradering } ?: emptyList()

        val skjermedeEgneAnsatte =
            bolkEgneAnsatteCache.getAsync(gjelderId) { _ ->
                skjermetClientService.isSkjermedePersonerInSkjermingslosningen(listOf(gjelderId))
            }

        return when {
            !saksbehandler.harTilgangTilFortrolig() && graderinger.contains(AdressebeskyttelseGradering.FORTROLIG) -> true
            !saksbehandler.harTilgangTilStrengtFortrolig() &&
                graderinger.intersect(listOf(AdressebeskyttelseGradering.STRENGT_FORTROLIG, AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND))
                    .isNotEmpty()
            -> true

            !saksbehandler.harTilgangTilEgneAnsatte() && skjermedeEgneAnsatte[gjelderId] == true -> true

            else -> false
        }
    }
}
