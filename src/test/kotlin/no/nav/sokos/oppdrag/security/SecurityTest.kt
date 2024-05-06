package no.nav.sokos.oppdrag.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import no.nav.sokos.oppdrag.common.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.common.config.PropertiesConfig
import no.nav.sokos.oppdrag.common.config.authenticate
import no.nav.sokos.oppdrag.common.config.configureSecurity
import no.nav.sokos.oppdrag.config.APPLICATION_JSON
import no.nav.sokos.oppdrag.config.BASE_API_PATH
import no.nav.sokos.oppdrag.config.OPPDRAGSINFO_API_PATH
import no.nav.sokos.oppdrag.config.configureTestApplication
import no.nav.sokos.oppdrag.oppdragsinfo.OppdragsInfoService
import no.nav.sokos.oppdrag.oppdragsinfo.api.model.GjelderIdRequest
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi

val oppdragsInfoService: OppdragsInfoService = mockk()

/**
 * Test for å sjekke at sikkerhetsmekanismen fungerer som forventet. Bruker
 * oppdragsinfo som utgangspunkt, fordi alle endepunktene i applikasjonen
 * er sikret under samme konfigurasjon. Endepunktene er wrappet i
 * en authenticate() funksjon som sjekker om bruker er autentisert.
 */

class SecurityTest : FunSpec({

    test("endepunkt uten token bør returnere 401") {
        withMockOAuth2Server {
            testApplication {
                configureTestApplication()
                this.application {
                    configureSecurity(authConfig())
                    routing {
                        authenticate(true, AUTHENTICATION_NAME) {
                            oppdragsInfoApi(oppdragsInfoService)
                        }
                    }
                }
                val response = client.post("$BASE_API_PATH$OPPDRAGSINFO_API_PATH/oppdrag")
                response.status shouldBe HttpStatusCode.Unauthorized
            }
        }
    }

    test("endepunkt med token bør returnere 200") {
        withMockOAuth2Server {
            testApplication {
                configureTestApplication()
                this.application {
                    configureSecurity(authConfig())
                    routing {
                        authenticate(true, AUTHENTICATION_NAME) {
                            oppdragsInfoApi(oppdragsInfoService)
                        }
                    }
                }

                coEvery { oppdragsInfoService.sokOppdrag(any(), any(), any()) } returns emptyList()

                val client =
                    createClient {
                        install(ContentNegotiation) {
                            json()
                        }
                    }

                val response =
                    client.post("$BASE_API_PATH$OPPDRAGSINFO_API_PATH/oppdrag") {
                        header(HttpHeaders.Authorization, "Bearer ${tokenFromDefaultProvider()}")
                        header(HttpHeaders.ContentType, APPLICATION_JSON)
                        setBody(GjelderIdRequest(gjelderId = "12345678901"))
                    }

                response.status shouldBe HttpStatusCode.OK
            }
        }
    }
})

private fun MockOAuth2Server.authConfig() =
    PropertiesConfig.AzureAdConfig(
        wellKnownUrl = wellKnownUrl("default").toString(),
        clientId = "default",
    )

private fun MockOAuth2Server.tokenFromDefaultProvider() =
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
