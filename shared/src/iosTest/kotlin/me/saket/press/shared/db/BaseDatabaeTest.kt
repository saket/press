package me.saket.press.shared.db

import co.touchlab.sqliter.DatabaseConfiguration
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.ios.NativeSqliteDriver
import com.squareup.sqldelight.drivers.ios.wrapConnection
import me.saket.press.PressDatabase

actual fun inMemorySqlDriver(): SqlDriver = NativeSqliteDriver(
    DatabaseConfiguration(
        name = "press.test.db",
        version = 1,
        inMemory = true,
        create = { connection ->
          wrapConnection(connection) { PressDatabase.Schema.create(it) }
        },
        upgrade = { connection, oldVersion, newVersion ->
          wrapConnection(connection) { PressDatabase.Schema.migrate(it, oldVersion, newVersion) }
        }
    )
)
