package no.nav.sokos.oppdrag.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object RedisConfig {

    private fun redisConfig(): RedisURI {
        val redisProperties: PropertiesConfig.RedisProperties = PropertiesConfig.RedisProperties()

        val redisURI =
            RedisURI
                .builder()
                .withHost(redisProperties.host)
                .withPort(redisProperties.port.toInt())
                .withSsl(true)
                .withAuthentication("", redisProperties.password)
                .build()
        return redisURI
    }

    fun getRedisClient(): RedisClient {
        val client = RedisClient.create(redisConfig())
        val result = client.connect().sync().ping()
        logger.info { "Connected to Redis: $result" }

        return client
    }
}