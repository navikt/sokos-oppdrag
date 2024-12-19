package no.nav.sokos.oppdrag.common.util

object CacheUtil {
    fun isFagSystemIdPartOfCacheKey(
        key: String,
        fagSystemId: String,
    ): Boolean {
        val regex = """^(?:[^-]*-){2}([^-]+(?:-[^-]+)*)-.*$""".toRegex()
        val match = regex.find(key)
        return match
            ?.groupValues
            ?.get(1)
            ?.let { fagSystemId.startsWith(it) } ?: false
    }
}
