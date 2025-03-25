package no.nav.sokos.oppdrag.listener

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.testcontainers.containers.GenericContainer

private const val VALKEY_PORT = 6379

object Valkeylistener : TestListener {
    private val valkeyContainer =
        GenericContainer("valkey:8.1-alpine")
            .withExposedPorts(VALKEY_PORT)
            .withReuse(true)

    lateinit var valkeyClient: RedisClient

    override suspend fun beforeSpec(spec: Spec) {
        valkeyContainer.start()
        valkeyClient =
            RedisClient.create(
                RedisURI
                    .builder()
                    .withHost(valkeyContainer.host)
                    .withPort(valkeyContainer.firstMappedPort)
                    .build(),
            )
    }

    override suspend fun afterSpec(spec: Spec) {
        valkeyContainer.stop()
    }
}
