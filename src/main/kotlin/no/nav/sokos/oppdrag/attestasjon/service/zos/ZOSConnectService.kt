package no.nav.sokos.oppdrag.attestasjon.service.zos

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import org.slf4j.MDC
import java.time.ZonedDateTime

class ZOSKlient(
    private val zOsUrl: String = PropertiesConfig.EksterneHostProperties().zosUrl,
    private val client: HttpClient = createHttpClient(false),
) {
    suspend fun attestereOppdrag(
        attestasjonRequest: AttestasjonRequest,
        navIdent: String,
    ): PostOSAttestasjonResponse200 {
        val response: HttpResponse =
            client.post("$zOsUrl/oppdaterAttestasjon") {
                header("Nav-Call-Id", MDC.get("x-correlation-id"))
                contentType(ContentType.Application.Json)
                setBody(mapToZosRequest(attestasjonRequest, navIdent))
            }

        return when {
            response.status.isSuccess() -> {
                val result = response.body<PostOSAttestasjonResponse200>()
                val attestasjonskvittering = result.osAttestasjonOperationResponse?.attestasjonskvittering?.responsAttestasjon
                if (attestasjonskvittering?.statuskode != 0) {
                    throw ZOSException(
                        ApiError(
                            ZonedDateTime.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            attestasjonskvittering?.melding ?: "Ukjent feil",
                            "$zOsUrl/oppdaterAttestasjon",
                        ),
                    )
                }
                result
            }

            else -> throw ZOSException(
                ApiError(
                    ZonedDateTime.now(),
                    response.status.value,
                    response.status.description,
                    "Message: ${response.errorMessage()}, Details: ${response.errorDetails()}",
                    "$zOsUrl/oppdaterAttestasjon",
                ),
            )
        }
    }

    private fun mapToZosRequest(
        request: AttestasjonRequest,
        navIdent: String,
    ): PostOSAttestasjonRequest {
        return PostOSAttestasjonRequest(
            osAttestasjonOperation =
                PostOSAttestasjonRequestOSAttestasjonOperation(
                    attestasjonsdata =
                        PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata(
                            requestAttestasjon =
                                PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjon(
                                    gjelderId = "x",
                                    fagomraade = "x",
                                    oppdragsId = request.oppdragsId,
                                    brukerId = "x",
                                    kjorIdag = true,
                                    linjeTab =
                                        request.linjer.map {
                                            PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjonLinjeTabInner(
                                                linjeId = it.linjeId,
                                                attestantId = navIdent,
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

data class ZOSException(val apiError: ApiError) : Exception(apiError.error)
