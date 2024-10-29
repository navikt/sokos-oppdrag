package no.nav.sokos.oppdrag.common.util

import com.github.benmanes.caffeine.cache.AsyncCache
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.supervisorScope

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
