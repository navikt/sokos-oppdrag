package no.nav.sokos.oppdrag.common.valkey

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerializationException

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.RedisCodec
import io.micrometer.core.instrument.Counter
import mu.KotlinLogging

import no.nav.sokos.oppdrag.config.ValkeyConfig
import no.nav.sokos.oppdrag.config.ValkeyConfig.useConnection

private val logger = KotlinLogging.logger {}

class ValkeyCache(
    private val name: String,
    private val cacheTTL: Duration = 10.minutes,
    private val valkeyClient: RedisClient = ValkeyConfig.getValkeyClient(),
) {
    private val cacheHit = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "hit").register(Metrics.prometheusMeterRegistryValkey)
    private val cacheError = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "error").register(Metrics.prometheusMeterRegistryValkey)
    private val cacheMiss = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "miss").register(Metrics.prometheusMeterRegistryValkey)

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun <T : Any> getAsync(
        key: String,
        codec: RedisCodec<String, T>,
        loader: suspend () -> T,
    ): T =
        valkeyClient.useConnection(codec) { connection ->
            try {
                connection.get(key)
            } catch (e: SerializationException) {
                cacheError.increment()
                logger.error("Deserialisering av cache entry feilet. Entry vil bli reloadet", e)
                null
            }?.also {
                cacheHit.increment()
            } ?: run {
                cacheMiss.increment()
                loader().also { connection.set(key, it, SetArgs.Builder.ex(cacheTTL.inWholeSeconds)) }
            }
        } ?: throw IllegalStateException("Failed to get value from cache")

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun delete(key: String) {
        valkeyClient.connect().use { connection ->
            connection.coroutines().del(key)
        }
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun getAllKeys(): List<String> =
        valkeyClient.connect().use { connection ->
            connection.coroutines().keys("*").toList()
        }
}
