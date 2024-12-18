package no.nav.sokos.oppdrag.listener

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.testcontainers.containers.GenericContainer

private const val REDIS_PORT = 6379

object RedisListener : TestListener {
    private val redisContainer =
        GenericContainer("redis:6.2.6")
            .withExposedPorts(REDIS_PORT)
            .withReuse(true)

    lateinit var redisClient: RedisClient

    override suspend fun beforeSpec(spec: Spec) {
        redisContainer.start()
        redisClient =
            RedisClient.create(
                RedisURI
                    .builder()
                    .withHost(redisContainer.host)
                    .withPort(redisContainer.firstMappedPort)
                    .build(),
            )
    }

    override suspend fun afterSpec(spec: Spec) {
        redisContainer.stop()
    }
}
