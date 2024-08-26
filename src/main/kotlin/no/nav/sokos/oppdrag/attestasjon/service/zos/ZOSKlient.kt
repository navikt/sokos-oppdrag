package no.nav.sokos.oppdrag.attestasjon.service.zos

import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyRequest
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyResponse200

interface ZOSKlient {
    suspend fun oppdaterAttestasjoner(request: PostOSAttestasjonDyRequest): PostOSAttestasjonDyResponse200
}
