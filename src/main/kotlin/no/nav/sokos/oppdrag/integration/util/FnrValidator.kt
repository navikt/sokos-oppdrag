package no.nav.sokos.oppdrag.integration.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val FNR_LENGTH = 11

object FnrValidator {
    fun String.isValidPid(): Boolean =
        when {
            (this.length != FNR_LENGTH || !this.all { it.isDigit() }) -> false
            !isValidDate(this.substring(0, 6)) -> false
            else -> true
        }

    private fun isValidDate(date: String): Boolean =
        runCatching {
            val formatter = DateTimeFormatter.ofPattern("ddMMyy")
            LocalDate.parse(date, formatter)
        }.isSuccess
}
