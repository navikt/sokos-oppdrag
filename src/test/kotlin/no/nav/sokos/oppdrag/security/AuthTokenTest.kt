package no.nav.sokos.oppdrag.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.TestUtil.tokenWithoutNavIdent
import org.junit.jupiter.api.assertThrows

private val engine = TestApplicationEngine(createTestEnvironment())

class AuthTokenTest : FunSpec({

    beforeTest {
        engine.start(wait = true)
    }

    test("token bør returnere NAVident") {

        val call =
            engine.handleRequest {
                addHeader(HttpHeaders.Authorization, "Bearer $tokenWithNavIdent")
            }

        val navIdent = AuthToken.getSaksbehandler(call)
        navIdent.ident shouldBe "Z123456"
    }

    test("token uten NAVident kaster en RuntimeException") {

        val call =
            engine.handleRequest {
                addHeader(HttpHeaders.Authorization, "Bearer $tokenWithoutNavIdent")
            }

        val exception =
            assertThrows<RuntimeException> {
                AuthToken.getSaksbehandler(call)
            }

        exception.message shouldBe "Missing NAVident in private claims"
    }

    test("mangler token i header kaster en Error") {
        val call =
            engine.handleRequest {}

        val exception =
            assertThrows<Error> {
                AuthToken.getSaksbehandler(call)
            }

        exception.message shouldBe "Could not get token from request header"
    }
})
