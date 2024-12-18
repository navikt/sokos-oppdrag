package no.nav.sokos.oppdrag.common.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.RedisCodec
import io.micrometer.core.instrument.Counter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.config.RedisConfig
import no.nav.sokos.oppdrag.config.RedisConfig.useConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

class RedisCache<T : Any>(
    private val name: String,
    private val cacheTTL: Duration = 10.minutes,
    private val redisClient: RedisClient = RedisConfig.getRedisClient(),
    private val codec: RedisCodec<String, T>,
) {
    private val cacheHit = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "hit").register(Metrics.prometheusMeterRegistryRedis)
    private val cacheError = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "error").register(Metrics.prometheusMeterRegistryRedis)
    private val cacheMiss = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "miss").register(Metrics.prometheusMeterRegistryRedis)

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun getAsync(
        key: String,
        loader: suspend () -> T,
    ): T =
        redisClient.useConnection(codec) { connection ->
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
        redisClient.connect().use { connection ->
            connection.coroutines().del(key)
        }
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun getAllKeys(): List<String> =
        redisClient.connect().use { connection ->
            connection.coroutines().keys("*").toList()
        }
}
