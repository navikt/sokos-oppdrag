package no.nav.sokos.oppdrag.attestasjon.service.zos

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
import org.slf4j.MDC

import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.api.model.ZosResponse
import no.nav.sokos.oppdrag.config.ApiError
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.createHttpClient
import no.nav.sokos.oppdrag.config.errorDetails
import no.nav.sokos.oppdrag.config.errorMessage

@OptIn(ExperimentalTime::class)
class ZOSConnectService(
    private val zOsUrl: String = PropertiesConfig.EksterneHostProperties().zosUrl,
    private val client: HttpClient = createHttpClient(false),
) {
    suspend fun attestereOppdrag(
        attestasjonRequest: AttestasjonRequest,
        navIdent: String,
    ): ZosResponse {
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
                val zOsResponse =
                    ZosResponse(
                        successMessage = "Oppdatering vellykket. ${attestasjonskvittering?.antLinjerMottatt} linjer oppdatert",
                    )
                if (attestasjonskvittering?.statuskode != 0) {
                    throw ZOSException(
                        ApiError(
                            Clock.System.now(),
                            HttpStatusCode.BadRequest.value,
                            HttpStatusCode.BadRequest.description,
                            attestasjonskvittering?.melding ?: "Ukjent feil",
                            "$zOsUrl/oppdaterAttestasjon",
                        ),
                    )
                }
                zOsResponse
            }

            else -> throw ZOSException(
                ApiError(
                    Clock.System.now(),
                    response.status.value,
                    response.status.description,
                    "Message: ${response.errorMessage()}, Details: ${response.errorDetails()}",
                    "$zOsUrl/oppdaterAttestasjon",
                ),
            )
        }
    }

    private fun AttestasjonRequest.mapToZosRequest(navIdent: String): PostOSAttestasjonRequest =
        PostOSAttestasjonRequest(
            PostOSAttestasjonRequestOSAttestasjonOperation(
                PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdata(
                    PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjon(
                        gjelderId,
                        kodeFagOmraade,
                        oppdragsId,
                        navIdent,
                        true,
                        linjer.map {
                            PostOSAttestasjonRequestOSAttestasjonOperationAttestasjonsdataRequestAttestasjonLinjeTabInner(
                                it.linjeId,
                                it.attestantIdent ?: navIdent,
                                it.datoUgyldigFom,
                            )
                        },
                    ),
                ),
            ),
        )
}

data class ZOSException(
    val apiError: ApiError,
) : Exception(apiError.error)
