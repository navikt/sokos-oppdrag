package no.nav.sokos.oppdrag.oppdragsinfo.util

import io.ktor.client.statement.HttpResponse
import no.nav.sokos.oppdrag.oppdragsinfo.config.ApiError

class EregException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)

class TpException(val apiError: ApiError, val response: HttpResponse) : Exception(apiError.error)

class OppdragsInfoException(val apiError: ApiError) : Exception(apiError.error)
