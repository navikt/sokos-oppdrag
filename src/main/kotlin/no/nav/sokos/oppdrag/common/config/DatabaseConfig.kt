package no.nav.sokos.oppdrag.common.config

import com.ibm.db2.jcc.DB2BaseDataSource
import com.ibm.db2.jcc.DB2SimpleDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class DatabaseConfig(
    private val db2Properties: PropertiesConfig.Db2Properties = PropertiesConfig.Db2Properties(),
) {
    private val dataSource: HikariDataSource = HikariDataSource(hikariConfig())

    val connection: Connection get() = dataSource.connection

    fun close() = dataSource.close()

    private fun hikariConfig() =
        HikariConfig().apply {
            maximumPoolSize = 10
            isAutoCommit = true
            poolName = "HikariPool-SOKOS-OPPDRAG"
            connectionTestQuery = "select 1 from sysibm.sysdummy1"
            dataSource =
                DB2SimpleDataSource().apply {
                    driverType = 4
                    enableNamedParameterMarkers = DB2BaseDataSource.YES
                    databaseName = db2Properties.name
                    serverName = db2Properties.host
                    portNumber = db2Properties.port.toInt()
                    currentSchema = db2Properties.schema
                    connectionTimeout = 1000
                    commandTimeout = 10000
                    user = db2Properties.username
                    setPassword(db2Properties.password)
                }
        }
}
