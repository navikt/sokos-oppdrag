package no.nav.sokos.oppdrag.common.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import io.micrometer.core.instrument.Counter
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

inline fun <reified T> createCodec(prefix: String): RedisCodec<String, T> {
    return object : RedisCodec<String, T> {
        private val stringCodec = StringCodec.UTF8
        private val namespace = "$prefix:"

        override fun decodeKey(bytes: ByteBuffer): String = stringCodec.decodeKey(bytes).removePrefix(namespace)

        override fun decodeValue(bytes: ByteBuffer): T =
            stringCodec.decodeValue(bytes).let {
                Json.decodeFromString(it)
            }

        override fun encodeKey(key: String): ByteBuffer = stringCodec.encodeKey("$namespace$key")

        override fun encodeValue(value: T): ByteBuffer =
            Json.encodeToString(value).let {
                stringCodec.encodeValue(it)
            }
    }
}

fun String.hashed() =
    String(MessageDigest.getInstance("SHA-256").digest(this.toByteArray()), charset("UTF-8"))

@OptIn(ExperimentalLettuceCoroutinesApi::class)
suspend fun <T : Any> RedisClient.useConnection(
    codec: RedisCodec<String, T>,
    body: suspend (RedisCoroutinesCommands<String, T>) -> T?
): T? {
    return this.connect(codec).use { connection ->
        val api = connection.coroutines()
        body(api)
    }
}

class RedisLoadingCache<T : Any>(
    name: String,
    private val redisClient: RedisClient,
    private val codec: RedisCodec<String, T>,
    private val loader: suspend (String) -> T,
    private val cacheTTL: Duration = 10.minutes,
) {
    private val cacheHit = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "hit").register(Metrics.prometheusMeterRegistryRedis)
    private val cacheError = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "error").register(Metrics.prometheusMeterRegistryRedis)
    private val cacheMiss = Counter.builder("sokos_oppdrag_redis_$name").tag("result", "miss").register(Metrics.prometheusMeterRegistryRedis)

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun get(key: String): T {
        return redisClient.useConnection(codec) { connection ->
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
                loader(key).also {
                    connection.set(key, it, SetArgs.Builder.ex(cacheTTL.inWholeMinutes))
                }
            }
        } ?: throw IllegalStateException("Failed to get value from cache")
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun update(key: String): T = redisClient.useConnection(codec) { connection ->
        loader(key).also {
            connection.set(key, it, SetArgs.Builder.ex(cacheTTL.inWholeMinutes))
        }
    } ?: throw IllegalStateException("Failed to update value from loader")
}