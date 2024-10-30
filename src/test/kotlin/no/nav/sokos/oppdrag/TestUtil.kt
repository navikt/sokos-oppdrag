package no.nav.sokos.oppdrag

import no.nav.sokos.oppdrag.common.audit.NavIdent

const val APPLICATION_JSON = "application/json"
const val INTEGRATION_BASE_API_PATH = "/api/v1/integration"
const val OPPDRAGSINFO_BASE_API_PATH = "/api/v1/oppdragsinfo"
const val ATTESTASJON_BASE_API_PATH = "/api/v1/attestasjon"

object TestUtil {
    fun String.readFromResource() = {}::class.java.classLoader.getResource(this)!!.readText()

    val navIdent = NavIdent("Z999999")
    val tokenWithNavIdent = "tokenWithNavIdent.txt".readFromResource()
    val tokenWithoutNavIdent = "tokenWithoutNavIdent.txt".readFromResource()
}
