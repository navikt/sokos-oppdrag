package no.nav.sokos.oppdrag.config

import java.io.File

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

import no.nav.sokos.oppdrag.security.AdGroup

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
                "ZOS_URL" to "http://155.55.1.82:9080/osattestasjonapi",
                "VALKEY_HOST" to "localhost",
                "VALKEY_PORT" to "6379",
                "VALKEY_PASSWORD" to "password",
                "MQ_HOSTNAME" to "10.53.17.118",
                "MQ_PORT" to "1413",
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
        val groupAccess: Map<String, String> =
            mapOf(
                getOrEmpty("GA_OKONOMI_EGNE_ANSATTE") to AdGroup.EGNE_ANSATTE.adGroupName,
                getOrEmpty("GA_OKONOMI_FORTROLIG") to AdGroup.FORTROLIG.adGroupName,
                getOrEmpty("GA_OKONOMI_STRENGT_FORTROLIG") to AdGroup.STRENGT_FORTROLIG.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NASJONALT_READ") to AdGroup.ATTESTASJON_NASJONALT_READ.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NASJONALT_WRITE") to AdGroup.ATTESTASJON_NASJONALT_WRITE.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NOP_READ") to AdGroup.ATTESTASJON_NOP_READ.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NOP_WRITE") to AdGroup.ATTESTASJON_NOP_WRITE.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NOS_READ") to AdGroup.ATTESTASJON_NOS_READ.adGroupName,
                getOrEmpty("GA_SOKOS_MF_ATTESTASJON_NOS_WRITE") to AdGroup.ATTESTASJON_NOS_WRITE.adGroupName,
                getOrEmpty("GA_SOKOS_MF_OPPDRAGSINFO_NASJONALT_READ") to AdGroup.OPPDRAGSINFO_NASJONALT_READ.adGroupName,
                getOrEmpty("GA_SOKOS_MF_OPPDRAGSINFO_NOP_READ") to AdGroup.OPPDRAGSINFO_NOP_READ.adGroupName,
                getOrEmpty("GA_SOKOS_MF_OPPDRAGSINFO_NOS_READ") to AdGroup.OPPDRAGSINFO_NOS_READ.adGroupName,
            ),
    )

    data class ValkeyProperties(
        val host: String = getOrEmpty("VALKEY_HOST"),
        val port: String = getOrEmpty("VALKEY_PORT"),
        val password: String = getOrEmpty("VALKEY_PASSWORD"),
        val ssl: Boolean = getOrEmpty("VALKEY_SSL").toBoolean(),
    )

    data class EksterneHostProperties(
        val eregUrl: String = getOrEmpty("EREG_URL"),
        val skjermetScope: String = getOrEmpty("SKJERMING_CLIENT_ID"),
        val skjermetUrl: String = getOrEmpty("SKJERMING_URL"),
        val pdlScope: String = getOrEmpty("PDL_SCOPE"),
        val pdlUrl: String = getOrEmpty("PDL_URL"),
        val zosUrl: String = getOrEmpty("ZOS_URL"),
    )

    data class MqProperties(
        val mqHostname: String = getOrEmpty("MQ_HOSTNAME"),
        val mqPort: Int = getOrEmpty("MQ_PORT").toInt(),
        val mqQueueManagerName: String = getOrEmpty("MQ_QUEUE_MANAGER_NAME"),
        val mqChannelName: String = getOrEmpty("MQ_CHANNEL_NAME"),
        val mqServiceUsername: String = getOrEmpty("MQ_SERVICE_USERNAME"),
        val mqServicePassword: String = getOrEmpty("MQ_SERVICE_PASSWORD"),
        val mqTssSamhandlerServiceQueue: String = getOrEmpty("MQ_TSS_SAMHANDLER_SERVICE_QUEUE"),
    )

    enum class Profile {
        LOCAL,
        DEV,
        PROD,
    }
}
