package no.nav.sokos.oppdrag.config

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import java.nio.ByteBuffer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object RedisConfig {
    private fun getRedisURI(): RedisURI {
        val redisProperties: PropertiesConfig.RedisProperties = PropertiesConfig.RedisProperties()

        val redisURI =
            RedisURI
                .builder()
                .withHost(redisProperties.host)
                .withPort(redisProperties.port.toInt())
                .withSsl(redisProperties.ssl)
                .withAuthentication("", redisProperties.password)
                .build()
        return redisURI
    }

    fun getRedisClient(redisURI: RedisURI = getRedisURI()): RedisClient {
        val client = RedisClient.create(redisURI)
        client.connect().use { connection ->
            logger.info { "Connected to Redis: ${connection.sync().ping()}" }
        }
        return client
    }

    inline fun <reified T> createCodec(prefix: String): RedisCodec<String, T> =
        object : RedisCodec<String, T> {
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

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun <T : Any> RedisClient.useConnection(
        codec: RedisCodec<String, T>,
        body: suspend (RedisCoroutinesCommands<String, T>) -> T?,
    ): T? =
        this.connect(codec).use { connection ->
            val api = connection.coroutines()
            body(api)
        }
}
