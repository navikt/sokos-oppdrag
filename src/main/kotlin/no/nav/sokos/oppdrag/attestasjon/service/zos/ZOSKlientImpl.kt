package no.nav.sokos.oppdrag.attestasjon.service.zos

import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import no.nav.sokos.oppdrag.config.httpClient
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyRequest
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyRequestOSAttestasjonDyOperation
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyRequestOSAttestasjonDyOperationAttestasjonsdata
import no.nav.sokos.oppdrag.model.PostOSAttestasjonDyResponse200

class ZOSKlientImpl : ZOSKlient {
    override suspend fun oppdaterAttestasjoner(request: PostOSAttestasjonDyRequest): PostOSAttestasjonDyResponse200 {
        httpClient.post("https://155.55.1.82:9080/osattestasjonapi") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            method = HttpMethod.Post
            setBody(PostOSAttestasjonDyRequest())
        }
        TODO("Not yet implemented")
    }

    private fun foo(request: PostOSAttestasjonDyRequestOSAttestasjonDyOperation) =
        PostOSAttestasjonDyRequestOSAttestasjonDyOperation(
            PostOSAttestasjonDyRequestOSAttestasjonDyOperationAttestasjonsdata(),
        )
}
