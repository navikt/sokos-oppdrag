package no.nav.sokos.oppdrag.common.util

import com.github.benmanes.caffeine.cache.AsyncCache
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.supervisorScope

object CacheUtil {
    suspend fun <K : Any, V : Any> AsyncCache<K, V>.getAsync(
        key: K,
        loader: suspend (K) -> V,
    ): V =
        supervisorScope {
            get(key) { key, _ ->
                future {
                    loader(key)
                }
            }.await()
        }

    fun isFagSystemIdPartOfCacheKey(
        key: String,
        fagSystemId: String,
    ): Boolean {
        val regex = """(?:[^-]+-){2}([^-]+)""".toRegex()
        val match = regex.find(key)
        return match
            ?.groupValues
            ?.get(1)
            ?.removeSuffix("%")
            ?.let { fagSystemId.startsWith(it) } ?: false
    }
}
