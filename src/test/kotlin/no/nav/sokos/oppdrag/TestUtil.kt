package no.nav.sokos.oppdrag

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sokos.oppdrag.config.PropertiesConfig

const val APPLICATION_JSON = "application/json"
const val OPPDRAGSINFO_BASE_API_PATH = "/api/v1/oppdragsinfo"

object TestUtil {
    private fun String.readFromResource() = {}::class.java.classLoader.getResource(this)!!.readText()

    val tokenWithNavIdent = "tokenWithNavIdent.txt".readFromResource()
    val tokenWithoutNavIdent = "tokenWithoutNavIdent.txt".readFromResource()

    fun MockOAuth2Server.mockAuthConfig() =
        PropertiesConfig.AzureAdProperties(
            wellKnownUrl = wellKnownUrl("default").toString(),
            clientId = "default",
        )
}
