package no.nav.sokos.oppdrag.integration.skjerming

interface SkjermetClient {
    suspend fun erPersonSkjermet(personIdent: String): Boolean
}
