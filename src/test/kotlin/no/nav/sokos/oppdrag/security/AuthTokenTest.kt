package no.nav.sokos.oppdrag.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.oppdrag.TestUtil.tokenWithNavIdent
import no.nav.sokos.oppdrag.TestUtil.tokenWithoutNavIdent
import org.junit.jupiter.api.assertThrows

internal class AuthTokenTest : FunSpec({

    test("token bør returnere NAVident") {

        val call = mockk<ApplicationCall>(relaxed = true)
        every { call.request.headers[HttpHeaders.Authorization] } returns "Bearer $tokenWithNavIdent"

        val result = AuthToken.getSaksbehandler(call)
        result.ident shouldBe "Z123456"
    }

    test("token uten NAVident kaster en RuntimeException") {

        val call = mockk<ApplicationCall>(relaxed = true)
        every { call.request.headers[HttpHeaders.Authorization] } returns "Bearer $tokenWithoutNavIdent"

        val result =
            assertThrows<RuntimeException> {
                AuthToken.getSaksbehandler(call)
            }

        result.message shouldBe "Missing NAVident in private claims"
    }

    test("mangler token i header kaster en Error") {

        val call = mockk<ApplicationCall>(relaxed = true)
        every { call.request.headers[HttpHeaders.Authorization] } returns null

        val exception =
            assertThrows<Error> {
                AuthToken.getSaksbehandler(call)
            }

        exception.message shouldBe "Could not get token from request header"
    }
})
