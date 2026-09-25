package no.nav.sokos.oppdrag.common.valkey

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerializationException

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.RedisCodec
import io.micrometer.core.instrument.Counter
import mu.KotlinLogging

import no.nav.sokos.oppdrag.config.ValkeyConfig

private val logger = KotlinLogging.logger {}

class ValkeyCache(
    private val name: String,
    private val cacheTTL: Duration = 10.minutes,
    private val valkeyClient: RedisClient = ValkeyConfig.getValkeyClient(),
) {
    private val cacheHit = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "hit").register(Metrics.prometheusMeterRegistryValkey)
    private val cacheError = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "error").register(Metrics.prometheusMeterRegistryValkey)
    private val cacheMiss = Counter.builder("sokos_oppdrag_valkey_$name").tag("result", "miss").register(Metrics.prometheusMeterRegistryValkey)

    // Lettuce-forbindelser er tenkt å være langlevde og trådsikre, og gjenoppretter selv ved tilkoblingsbrudd.
    // Derfor caches én tilkobling per codec og gjenbrukes for hvert kall, i stedet for å åpne/lukke en ny
    // (og dermed gjøre et nytt DNS-oppslag) for hvert cache-oppslag.
    private val connectionsByCodec = HashMap<RedisCodec<String, *>, StatefulRedisConnection<String, *>>()
    private val defaultConnection: StatefulRedisConnection<String, String> by lazy { valkeyClient.connect() }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    private fun <T : Any> connectionFor(codec: RedisCodec<String, T>): StatefulRedisConnection<String, T> =
        connectionsByCodec.getOrPut(codec) { valkeyClient.connect(codec) } as StatefulRedisConnection<String, T>

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun <T : Any> getAsync(
        key: String,
        codec: RedisCodec<String, T>,
        loader: suspend () -> T,
    ): T {
        val api = connectionFor(codec).coroutines()
        return try {
            api.get(key)
        } catch (e: SerializationException) {
            cacheError.increment()
            logger.error("Deserialisering av cache entry feilet. Entry vil bli reloadet", e)
            null
        }?.also {
            cacheHit.increment()
        } ?: run {
            cacheMiss.increment()
            loader().also { api.set(key, it, SetArgs.Builder.ex(cacheTTL.inWholeSeconds)) }
        }
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun delete(key: String) {
        defaultConnection.coroutines().del(key)
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun getAllKeys(): List<String> = defaultConnection.coroutines().keys("*").toList()
}
