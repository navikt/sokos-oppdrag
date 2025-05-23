package no.nav.sokos.oppdrag.security

import kotlinx.serialization.json.Json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.sokos.oppdrag.attestasjon.APPLICATION_JSON
import no.nav.sokos.oppdrag.attestasjon.OPPDRAGSINFO_BASE_API_PATH
import no.nav.sokos.oppdrag.common.dto.WrappedReponseWithErrorDTO
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.PropertiesConfig
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.config.securityConfig
import no.nav.sokos.oppdrag.integration.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi
import no.nav.sokos.oppdrag.oppdragsinfo.service.OppdragsInfoService

val oppdragsInfoService = mockk<OppdragsInfoService>()

/**
 * Test for å sjekke at sikkerhetsmekanismen fungerer som forventet. Bruker
 * kodeverk som utgangspunkt, fordi alle endepunktene i applikasjonen
 * er sikret under samme konfigurasjon. Endepunktene er wrappet i
 * en authenticate() funksjon som sjekker om bruker er autentisert.
 */

internal class SecurityTest :
    FunSpec({

        test("http post til sikker endepunkt uten token bør returnere 401") {
            withMockOAuth2Server {
                testApplication {
                    application {
                        securityConfig(true, mockAuthConfig())
                        routing {
                            authenticate(true, AUTHENTICATION_NAME) {
                                oppdragsInfoApi(oppdragsInfoService)
                            }
                        }
                    }
                    val response = client.get("$OPPDRAGSINFO_BASE_API_PATH/12345676544/oppdragslinjer")
                    response.status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        test("http post til sikker endepunkt med token bør returnere 200") {
            withMockOAuth2Server {
                testApplication {
                    application {
                        commonConfig()
                        securityConfig(true, mockAuthConfig())
                        routing {
                            authenticate(true, AUTHENTICATION_NAME) {
                                oppdragsInfoApi(oppdragsInfoService)
                            }
                        }
                    }

                    coEvery { oppdragsInfoService.getOppdrag(any(), any(), any()) } returns WrappedReponseWithErrorDTO()

                    val client =
                        createClient {
                            install(ContentNegotiation) {
                                json(
                                    Json {
                                        prettyPrint = true
                                        ignoreUnknownKeys = true
                                        encodeDefaults = true
                                        explicitNulls = false
                                    },
                                )
                            }
                        }

                    val response =
                        client.post("$OPPDRAGSINFO_BASE_API_PATH/sok") {
                            header(HttpHeaders.Authorization, "Bearer ${token()}")
                            header(HttpHeaders.ContentType, APPLICATION_JSON)
                            setBody(GjelderIdRequest("12345678901"))
                        }

                    response.status shouldBe HttpStatusCode.OK
                }
            }
        }
    })

private fun MockOAuth2Server.token() =
    issueToken(
        issuerId = "default",
        clientId = "default",
        tokenCallback =
            DefaultOAuth2TokenCallback(
                claims =
                    mapOf(
                        "NAVident" to "Z123456",
                    ),
            ),
    ).serialize()

private fun MockOAuth2Server.mockAuthConfig() =
    PropertiesConfig.AzureAdProperties(
        wellKnownUrl = wellKnownUrl("default").toString(),
        clientId = "default",
    )
