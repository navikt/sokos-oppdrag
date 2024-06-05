package no.nav.sokos.oppdrag.security

import com.auth0.jwt.JWT
import io.ktor.server.application.ApplicationCall
import no.nav.sokos.oppdrag.common.audit.NavIdent

const val JWT_CLAIM_NAVIDENT = "NAVident"

object JwtClaimHandler {
    fun getSaksbehandler(call: ApplicationCall): NavIdent {
        val oboToken =
            call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: throw IllegalStateException("Could not get token from request header")
        val navIdent = getNAVIdentFromToken(oboToken)

        return NavIdent(navIdent)
    }

    private fun getNAVIdentFromToken(token: String): String {
        val decodedJWT = JWT.decode(token)
        return decodedJWT.claims[JWT_CLAIM_NAVIDENT]?.asString()
            ?: throw RuntimeException("Missing NAVident in private claims")
    }
}
