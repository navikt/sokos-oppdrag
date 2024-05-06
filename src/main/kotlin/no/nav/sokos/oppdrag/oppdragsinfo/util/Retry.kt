package no.nav.sokos.oppdrag.oppdragsinfo.util

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun <T> retry(
    numberOfTries: Int = 5,
    interval: Duration = 250.milliseconds,
    block: suspend () -> T,
): T {
    var attempt = 0
    var error: Throwable?
    do {
        try {
            return block()
        } catch (e: Throwable) {
            error = e
        }
        attempt++
        delay(interval)
    } while (attempt < numberOfTries)

    throw error ?: RetryException("Retry failed without error")
}

class RetryException(override val message: String) : Exception(message)
