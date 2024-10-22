package no.nav.sokos.oppdrag.security

import com.auth0.jwt.JWT
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import java.util.*
import no.nav.sokos.oppdrag.common.audit.NavIdent
import no.nav.sokos.oppdrag.config.PropertiesConfig

const val JWT_CLAIM_NAVIDENT = "NAVident"
const val JWT_CLAIM_GROUPS = "groups"


object AuthToken {
    fun getSaksbehandler(call: ApplicationCall): NavIdent {
        val oboToken =
            call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                ?: throw Error("Could not get token from request header")
        val navIdent = getNAVIdentFromToken(oboToken)
        val groupUUIDs = getGroupsFromToken(oboToken)
        val rolleMap = PropertiesConfig.AzureAdProperties().rolleMap
        val groups = groupUUIDs.mapNotNull(rolleMap::get)

        return NavIdent(navIdent, groups)
    }

    private fun getNAVIdentFromToken(token: String): String {
        val decodedJWT = JWT.decode(token)
        return decodedJWT.claims[JWT_CLAIM_NAVIDENT]?.asString()
            ?: throw RuntimeException("Missing NAVident in private claims")
    }

    private fun getGroupsFromToken(token: String): List<UUID> {
        val decodedJWT = JWT.decode(token)
        return decodedJWT.claims[JWT_CLAIM_GROUPS]?.asList(UUID::class.java)
            ?: throw RuntimeException("Missing Groups in private claims")
    }

}
