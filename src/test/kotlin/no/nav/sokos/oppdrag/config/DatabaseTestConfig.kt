package no.nav.sokos.oppdrag.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.TransactionalSession
import kotliquery.sessionOf
import kotliquery.using

object DatabaseTestConfig {
    fun hikariConfig() =
        HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:test_mottak;MODE=DB2;DB_CLOSE_DELAY=-1;"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 10
            validate()
        }
}

fun <A> HikariDataSource.transaction(operation: (TransactionalSession) -> A): A =
    using(sessionOf(this, returnGeneratedKey = true)) { session ->
        session.transaction { tx ->
            operation(tx)
        }
    }
