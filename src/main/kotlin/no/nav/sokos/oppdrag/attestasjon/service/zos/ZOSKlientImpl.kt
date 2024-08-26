package no.nav.sokos.oppdrag.attestasjon.service.zos

import io.ktor.client.request.post
import no.nav.sokos.oppdrag.config.httpClient
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyRequest
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyResponse200

class ZOSKlientImpl : ZOSKlient {
    override suspend fun oppdaterAttestasjoner(request: PostOSAttestasjonDyRequest): PostOSAttestasjonDyResponse200 {
        httpClient.post("")
        TODO("Not yet implemented")
    }
}
