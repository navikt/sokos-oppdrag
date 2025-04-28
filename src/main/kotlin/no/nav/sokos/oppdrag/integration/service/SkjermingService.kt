package no.nav.sokos.oppdrag.integration.service

import no.nav.pdl.enums.AdressebeskyttelseGradering
import no.nav.pdl.hentpersonbolk.Person
import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.common.valkey.ValkeyCache
import no.nav.sokos.oppdrag.config.ValkeyConfig.createCodec
import no.nav.sokos.oppdrag.integration.client.pdl.PdlClientService
import no.nav.sokos.oppdrag.integration.client.skjerming.SkjermetClientService
import no.nav.sokos.oppdrag.security.AdGroup

class SkjermingService(
    private val pdlClientService: PdlClientService = PdlClientService(),
    private val skjermetClientService: SkjermetClientService = SkjermetClientService(),
    private val valkeyCache: ValkeyCache = ValkeyCache(name = "skjermingService"),
) {
    suspend fun getSkjermingForIdentListe(
        identer: List<String>,
        navIdent: NavIdent,
    ): Map<String, Boolean> {
        val deduplicatedIdenter = identer.distinct()
        val personIdenter = deduplicatedIdenter.filter { it.toLong() in 1_000_000_001..79_999_999_999 }

        if (personIdenter.isEmpty()) {
            return deduplicatedIdenter.associateWith { false }
        }

        val egenAnsattMap =
            valkeyCache
                .getAsync(key = personIdenter.joinToString(), codec = createCodec<Map<String, Boolean>>("hent-egne-ansatte")) {
                    skjermetClientService.isSkjermedePersonerInSkjermingslosningen(personIdenter)
                }.mapValues { (_, skjermet) -> !navIdent.hasAdGroupAccess(AdGroup.EGNE_ANSATTE.adGroupName) && skjermet }

        val adressebeskyttelseMap =
            valkeyCache
                .getAsync(key = personIdenter.joinToString(), codec = createCodec<Map<String, Person>>("hent-pdl")) {
                    pdlClientService.getPerson(identer = personIdenter)
                }.mapValues { (_, person) ->
                    val graderinger = person.adressebeskyttelse.map { it.gradering }
                    !navIdent.hasAdGroupAccess(AdGroup.FORTROLIG.adGroupName) &&
                        AdressebeskyttelseGradering.FORTROLIG in graderinger ||
                        !navIdent.hasAdGroupAccess(AdGroup.STRENGT_FORTROLIG.adGroupName) &&
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
        navIdent: NavIdent,
    ): Boolean = getSkjermingForIdentListe(listOf(gjelderId), navIdent)[gjelderId] ?: false
}
