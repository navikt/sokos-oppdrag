package no.nav.sokos.oppdrag.attestasjon.exception

data class AttestasjonException(override val message: String) : Exception(message)
