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
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.exception.ZOSException
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.config.errorDetails
import no.nav.sokos.oppdrag.config.errorMessage
import org.slf4j.MDC
import java.time.ZonedDateTime

class ZOSConnectService(
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
                setBody(attestasjonRequest.mapToZosRequest(navIdent))
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

    private fun AttestasjonRequest.mapToZosRequest(navIdent: String): PostOSAttestasjonRequest {
        return PostOSAttestasjonRequest(
            osAttestasjonOperation =
                PostOSAttestasjonRequestOSAttestasjonOperation(
                    attestasjonsdata =
                        PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata(
                            requestAttestasjon =
                                PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjon(
                                    gjelderId = gjelderId,
                                    fagomraade = kodeFagOmraade,
                                    oppdragsId = oppdragsId,
                                    brukerId = navIdent,
                                    kjorIdag = true,
                                    linjeTab =
                                        linjer.map {
                                            PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjonLinjeTabInner(
                                                linjeId = it.linjeId,
                                                attestantId = it.attestantIdent ?: navIdent,
                                                datoUgyldigFom = it.datoUgyldigFom,
                                            )
                                        },
                                ),
                        ),
                ),
        )
    }
}
