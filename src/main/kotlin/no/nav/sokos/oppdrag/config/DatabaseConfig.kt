package no.nav.sokos.oppdrag.config

import java.time.Duration

import com.ibm.db2.jcc.DB2BaseDataSource
import com.ibm.db2.jcc.DB2SimpleDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DatabaseConfig {
    val db2DataSource: HikariDataSource by lazy {
        HikariDataSource(db2HikariConfig())
    }

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                db2DataSource.close()
            },
        )
    }

    private fun db2HikariConfig(): HikariConfig {
        val db2Properties: PropertiesConfig.Db2Properties = PropertiesConfig.Db2Properties()
        return HikariConfig().apply {
            minimumIdle = 1
            maximumPoolSize = 10
            connectionTimeout = Duration.ofSeconds(5).toMillis()
            idleTimeout = Duration.ofMinutes(5).toMillis()
            keepaliveTime = Duration.ofMinutes(5).toMillis()
            maxLifetime = Duration.ofMinutes(30).toMillis()

            connectionTestQuery = "select 1 from sysibm.sysdummy1"
            validationTimeout = Duration.ofSeconds(5).toMillis()

            dataSource =
                DB2SimpleDataSource().apply {
                    driverType = 4
                    enableNamedParameterMarkers = DB2BaseDataSource.YES
                    databaseName = db2Properties.name
                    serverName = db2Properties.host
                    portNumber = db2Properties.port.toInt()
                    currentSchema = db2Properties.schema
                    user = db2Properties.username
                    setPassword(db2Properties.password)
                }
        }
    }
}
