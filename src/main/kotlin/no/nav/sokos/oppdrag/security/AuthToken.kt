package no.nav.sokos.oppdrag.security

import com.auth0.jwt.JWT
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

import no.nav.sokos.oppdrag.common.NavIdent
import no.nav.sokos.oppdrag.config.PropertiesConfig

const val JWT_CLAIM_NAVIDENT = "NAVident"
const val JWT_CLAIM_GROUPS = "groups"

object AuthToken {
    fun getSaksbehandler(call: ApplicationCall): NavIdent {
        val oboToken =
            call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                ?: throw Error("Could not get token from request header")

        val navIdent = getNAVIdentFromToken(oboToken)
        val groupsFromOboToken = getGroupsFromToken(oboToken)
        val groupAccess = PropertiesConfig.AzureAdProperties().groupAccess
        val groups = groupsFromOboToken.mapNotNull { groupAccess[it] }

        return NavIdent(navIdent, groups)
    }

    private fun getNAVIdentFromToken(token: String): String {
        val decodedJWT = JWT.decode(token)
        return decodedJWT.claims[JWT_CLAIM_NAVIDENT]?.asString()
            ?: throw RuntimeException("Missing NAVident in private claims")
    }

    private fun getGroupsFromToken(token: String): List<String> {
        val decodedJWT = JWT.decode(token)
        return decodedJWT.claims[JWT_CLAIM_GROUPS]?.asList(String::class.java) ?: emptyList()
    }
}
