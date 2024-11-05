package no.nav.sokos.oppdrag.integration.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val FNR_LENGTH = 11
private const val DNR_LENGTH = 40

object FnrValidator {
    fun String.isValidPid(): Boolean =
        when {
            (this.length != FNR_LENGTH || !this.all { it.isDigit() }) -> false
            !isValidDate(this.substring(0, 6)) -> false
            else -> true
        }

    private fun isValidDate(fnr: String): Boolean {
        if (isDNumber(fnr) || isDollyNumber(fnr)) return true

        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("ddMMyy")
            LocalDate.parse(fnr, formatter)
        }.isSuccess
    }

    private fun isDNumber(fnr: String): Boolean = fnr.substring(0, 2).toInt() in (DNR_LENGTH + 1)..71

    private fun isDollyNumber(fnr: String): Boolean = fnr.substring(2, 4).toInt() in (DNR_LENGTH + 1)..52
}
