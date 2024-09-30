package no.nav.sokos.oppdrag.attestasjon.exception

import no.nav.sokos.oppdrag.config.ApiError

data class ZOSException(val apiError: ApiError) : Exception(apiError.error)
