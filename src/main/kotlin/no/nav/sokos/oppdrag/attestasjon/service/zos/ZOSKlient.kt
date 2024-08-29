package no.nav.sokos.oppdrag.attestasjon.service.zos

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonsRequest
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.httpClient
import java.time.ZonedDateTime

class ZOSKlient {
    suspend fun update(request: AttestasjonsRequest): PostOSAttestasjonResponse200 {
        val zosRequest: PostOSAttestasjonRequest = mapToZosRequest(request)
        val url = "${PropertiesConfig.EksterneHostProperties().zosUrl}/oppdaterAttestasjon"

        val response: HttpResponse =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(zosRequest)
            }

        return when {
            response.status.isSuccess() -> response.body<PostOSAttestasjonResponse200>()
            else ->
                throw ZOSException(
                    ApiError(
                        ZonedDateTime.now(),
                        response.status.value,
                        response.status.description,
                        "Message: ${response.errorMessage() }, Details: ${response.errorDetails()}",
                        url,
                    ),
                    response,
                )
        }
    }

    private fun mapToZosRequest(request: AttestasjonsRequest): PostOSAttestasjonRequest {
        return PostOSAttestasjonRequest(
            osAttestasjonOperation =
                PostOSAttestasjonRequestOSAttestasjonOperation(
                    attestasjonsdata =
                        PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata(
                            requestAttestasjon =
                                PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjon(
                                    gjelderId = request.gjelderId,
                                    fagomraade = request.fagOmraade,
                                    oppdragsId = request.oppdragsId,
                                    brukerId = request.brukerId,
                                    kjorIdag = request.kjorIdag,
                                    linjeTab =
                                        request.linjer.map {
                                            PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjonLinjeTabInner(
                                                linjeId = it.linjeId,
                                                attestantId = it.attestantId,
                                                datoUgyldigFom = it.datoUgyldigFom,
                                            )
                                        },
                                ),
                        ),
                ),
        )
    }
}

suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["errorMessage"]?.jsonPrimitive?.content

suspend fun HttpResponse.errorDetails() = body<JsonElement>().jsonObject["errorDetails"]?.jsonPrimitive?.content

data class ZOSException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)
