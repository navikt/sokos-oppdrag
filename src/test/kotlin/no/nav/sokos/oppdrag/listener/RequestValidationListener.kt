@file:Suppress("UNCHECKED_CAST")

package no.nav.sokos.oppdrag.listener

import io.kotest.core.listeners.TestListener
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.Validator
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object RequestValidationListener : TestListener {
    private val validatorsField = RequestValidationConfig::class.declaredMemberProperties.find { it.name == "validators" }

    fun getValidators(config: RequestValidationConfig): MutableList<Validator> {
        validatorsField?.isAccessible = true
        return validatorsField?.get(config) as MutableList<Validator>
    }
}
