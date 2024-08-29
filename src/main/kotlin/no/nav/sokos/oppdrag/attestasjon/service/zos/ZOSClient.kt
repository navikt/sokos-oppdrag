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
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.httpClient
import java.time.ZonedDateTime

class ZOSKlient(
    private val zOsUrl: String = PropertiesConfig.EksterneHostProperties().zosUrl,
) {
    suspend fun attestereOppdrag(attestasjonRequest: AttestasjonRequest): PostOSAttestasjonResponse200 {
        val response: HttpResponse =
            httpClient.post("$zOsUrl/oppdaterAttestasjon") {
                contentType(ContentType.Application.Json)
                setBody(mapToZosRequest(attestasjonRequest))
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
                        "$zOsUrl/oppdaterAttestasjon",
                    ),
                    response,
                )
        }
    }

    private fun mapToZosRequest(request: AttestasjonRequest): PostOSAttestasjonRequest {
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

private suspend fun HttpResponse.errorMessage() = body<JsonElement>().jsonObject["errorMessage"]?.jsonPrimitive?.content

private suspend fun HttpResponse.errorDetails() = body<JsonElement>().jsonObject["errorDetails"]?.jsonPrimitive?.content

data class ZOSException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)
