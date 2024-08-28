package no.nav.sokos.oppdrag.attestasjon.service.zos

import no.nav.sokos.oppdrag.model.PostOSAttestasjonRequest
import no.nav.sokos.oppdrag.model.PostOSAttestasjonResponse200

interface ZOSKlient {
    suspend fun oppdaterAttestasjoner(request: PostOSAttestasjonRequest): PostOSAttestasjonResponse200
}
