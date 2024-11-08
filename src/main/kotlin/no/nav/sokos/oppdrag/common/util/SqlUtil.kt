package no.nav.sokos.oppdrag.common.util

object SqlUtil {
    fun String.sanitizeForSql(): String {
        val regex = Regex("[;'\"\\-\\-]|/\\*|\\*/|xp_|exec|drop|insert|select|delete|update|alter|create|shutdown|grant|revoke|union", RegexOption.IGNORE_CASE)
        return regex.replace(this, "")
    }
}
