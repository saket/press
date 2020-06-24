package me.saket.press.shared.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import me.saket.press.PressDatabase

actual fun inMemorySqlDriver(): SqlDriver =
  JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
    PressDatabase.Schema.create(this)
  }
