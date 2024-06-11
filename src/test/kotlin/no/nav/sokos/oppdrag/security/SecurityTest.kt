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
import io.mockk.every
import io.mockk.mockk
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.sokos.oppdrag.APPLICATION_JSON
import no.nav.sokos.oppdrag.OPPDRAGSINFO_BASE_API_PATH
import no.nav.sokos.oppdrag.TestUtil.mockAuthConfig
import no.nav.sokos.oppdrag.common.model.GjelderIdRequest
import no.nav.sokos.oppdrag.config.AUTHENTICATION_NAME
import no.nav.sokos.oppdrag.config.authenticate
import no.nav.sokos.oppdrag.config.commonConfig
import no.nav.sokos.oppdrag.config.securityConfig
import no.nav.sokos.oppdrag.oppdragsinfo.api.oppdragsInfoApi
import no.nav.sokos.oppdrag.oppdragsinfo.service.OppdragsInfoService

val oppdragsInfoService: OppdragsInfoService = mockk()

/**
 * Test for å sjekke at sikkerhetsmekanismen fungerer som forventet. Bruker
 * oppdragsinfo som utgangspunkt, fordi alle endepunktene i applikasjonen
 * er sikret under samme konfigurasjon. Endepunktene er wrappet i
 * en authenticate() funksjon som sjekker om bruker er autentisert.
 */

internal class SecurityTest : FunSpec({

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
                val response = client.post("$OPPDRAGSINFO_BASE_API_PATH/oppdrag")
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

                every { oppdragsInfoService.sokOppdragsInfo(any(), any(), any()) } returns emptyList()

                val client =
                    createClient {
                        install(ContentNegotiation) {
                            json()
                        }
                    }

                val response =
                    client.post("$OPPDRAGSINFO_BASE_API_PATH/oppdragsinfo") {
                        header(HttpHeaders.Authorization, "Bearer ${token()}")
                        header(HttpHeaders.ContentType, APPLICATION_JSON)
                        setBody(GjelderIdRequest(gjelderId = "12345678901"))
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
            DefaultOAuth2TokenCallback(),
    ).serialize()
