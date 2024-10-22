package no.nav.sokos.oppdrag.config

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import java.io.File
import java.util.UUID

object PropertiesConfig {
    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "NAIS_APP_NAME" to "sokos-oppdrag",
                "NAIS_NAMESPACE" to "okonomi",
                "USE_AUTHENTICATION" to "true",
            ),
        )

    private val localDevProperties =
        ConfigurationMap(
            mapOf(
                "APPLICATION_PROFILE" to Profile.LOCAL.toString(),
                "USE_AUTHENTICATION" to "false",
            ),
        )

    private val devProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.DEV.toString()))
    private val prodProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.PROD.toString()))

    private val config =
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding devProperties overriding defaultProperties
            "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding prodProperties overriding defaultProperties
            else ->
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding
                        ConfigurationProperties.fromOptionalFile(
                            File("defaults.properties"),
                        ) overriding localDevProperties overriding defaultProperties
        }

    operator fun get(key: String): String = config[Key(key, stringType)]

    fun getOrEmpty(key: String): String = config.getOrElse(Key(key, stringType), "")

    data class Configuration(
        val naisAppName: String = get("NAIS_APP_NAME"),
        val profile: Profile = Profile.valueOf(get("APPLICATION_PROFILE")),
        val useAuthentication: Boolean = get("USE_AUTHENTICATION").toBoolean(),
        val azureAdProperties: AzureAdProperties = AzureAdProperties(),
    )

    data class Db2Properties(
        val host: String = getOrEmpty("DATABASE_HOST"),
        val port: String = getOrEmpty("DATABASE_PORT"),
        val name: String = getOrEmpty("DATABASE_NAME"),
        val schema: String = getOrEmpty("DATABASE_SCHEMA"),
        val username: String = getOrEmpty("DATABASE_USERNAME"),
        val password: String = getOrEmpty("DATABASE_PASSWORD"),
    )

    data class AzureAdProperties(
        val clientId: String = getOrEmpty("AZURE_APP_CLIENT_ID"),
        val wellKnownUrl: String = getOrEmpty("AZURE_APP_WELL_KNOWN_URL"),
        val tenantId: String = getOrEmpty("AZURE_APP_TENANT_ID"),
        val clientSecret: String = getOrEmpty("AZURE_APP_CLIENT_SECRET"),
        val rolleMap: Map<UUID, String> =
            mapOf(
//                UUID.fromString(get("UUID_ROLLE_EGNE_ANSATTE")) to GRUPPE_EGNE_ANSATTE,
//                UUID.fromString(get("UUID_ROLLE_FORTROLIG")) to GRUPPE_FORTROLIG,
//                UUID.fromString(get("UUID_ROLLE_STRENGT_FORTROLIG")) to GRUPPE_STRENGT_FORTROLIG,
//                UUID.fromString(get("UUID_ROLLE_ATTESTASJON_SKRIV")) to GRUPPE_ATTESTASJON_SKRIV,
            ),
    )

    data class EksterneHostProperties(
        val pdlUrl: String = getOrEmpty("PDL_URL"),
        val pdlScope: String = getOrEmpty("PDL_SCOPE"),
        val eregUrl: String = getOrEmpty("EREG_URL"),
        val tpUrl: String = getOrEmpty("TP_URL"),
        val zosUrl: String = getOrEmpty("ZOS_URL"),
    )

    enum class Profile {
        LOCAL,
        DEV,
        PROD,
    }
}
