package no.nav.sokos.oppdrag.listener

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener

object WiremockListener : AbstractProjectConfig() {
    private const val WIREMOCK_SERVER_PORT = 9001

    val wiremock = WireMockServer(WIREMOCK_SERVER_PORT)
    private val wiremockListener = WireMockListener(wiremock, ListenerMode.PER_PROJECT)

    override fun extensions() = listOf(wiremockListener)

    override suspend fun beforeProject() {
        configureFor(WIREMOCK_SERVER_PORT)
    }
}
