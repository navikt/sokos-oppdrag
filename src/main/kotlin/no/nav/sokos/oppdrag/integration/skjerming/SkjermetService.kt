package no.nav.sokos.oppdrag.integration.skjerming

import kotlinx.coroutines.runBlocking
import no.nav.sokos.oppdrag.common.audit.NavIdent

class SkjermetService(
    private val skjermetClient: SkjermetClient,
) {
    fun kanSaksbehandlerSePerson(
        personIdent: String,
        saksbehandler: NavIdent,
    ): Boolean {
        return saksbehandler.harTilgangTilEgneAnsatte() || !erPersonSkjermet(personIdent)
    }

    private fun erPersonSkjermet(personIdent: String): Boolean {
        return runBlocking {
            skjermetClient.erPersonSkjermet(personIdent)
        }
    }
}
