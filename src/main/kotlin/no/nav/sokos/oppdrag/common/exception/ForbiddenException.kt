package no.nav.sokos.oppdrag.common.exception

data class ForbiddenException(override val message: String) : Exception(message)
