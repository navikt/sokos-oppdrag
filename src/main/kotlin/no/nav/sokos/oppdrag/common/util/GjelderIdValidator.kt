package no.nav.sokos.oppdrag.common.util

object GjelderIdValidator {
    fun isValidGjelderId(gjelderId: String): Boolean = Regex("^(\\d{11}|\\d{9})\$").matches(gjelderId)
}
