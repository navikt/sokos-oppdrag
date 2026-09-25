package no.nav.sokos.oppdrag.config

import java.nio.ByteBuffer

import kotlinx.serialization.json.Json

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object ValkeyConfig {
    private fun getValkeyURI(): RedisURI {
        val valkeyProperties: PropertiesConfig.ValkeyProperties = PropertiesConfig.ValkeyProperties()

        val valkeyURI =
            RedisURI
                .builder()
                .withHost(valkeyProperties.host)
                .withPort(valkeyProperties.port.toInt())
                .withSsl(valkeyProperties.ssl)
                .withPassword(valkeyProperties.password.toCharArray())
                .build()
        return valkeyURI
    }

    // Én RedisClient deles på tvers av alle ValkeyCache-instanser i applikasjonen. Lettuce anbefaler å
    // gjenbruke én klient per applikasjon, siden hver klient eier egne Netty-tråder/ressurser.
    private val sharedValkeyClient: RedisClient by lazy {
        val client = RedisClient.create(getValkeyURI())
        client.connect().use { connection ->
            logger.info { "Connected to Valkey: ${connection.sync().ping()}" }
        }
        client
    }

    fun getValkeyClient(): RedisClient = sharedValkeyClient

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
}
