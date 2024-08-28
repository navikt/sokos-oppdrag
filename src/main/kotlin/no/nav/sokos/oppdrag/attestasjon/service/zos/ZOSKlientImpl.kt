package no.nav.sokos.oppdrag.attestasjon.service.zos

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.sokos.oppdrag.config.httpClient
import no.nav.sokos.oppdrag.model.PostOSAttestasjonRequest
import no.nav.sokos.oppdrag.model.PostOSAttestasjonRequestOSAttestasjonOperation
import no.nav.sokos.oppdrag.model.PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata
import no.nav.sokos.oppdrag.model.PostOSAttestasjonResponse200

class ZOSKlientImpl : ZOSKlient {
    override suspend fun oppdaterAttestasjoner(request: PostOSAttestasjonRequest): PostOSAttestasjonResponse200 {
        httpClient.post("https://155.55.1.82:9080/osattestasjonapi") {
            contentType(ContentType.Application.Json)
            setBody(PostOSAttestasjonRequest())
        }
        TODO("Not yet implemented")
    }

    private fun foo(request: PostOSAttestasjonRequestOSAttestasjonOperation) =
        PostOSAttestasjonRequestOSAttestasjonOperation(
            PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata(),
        )
}
