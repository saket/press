package me.saket.press.shared.di

import com.russhwolf.settings.Settings
import com.squareup.sqldelight.db.SqlDriver

/**
 * @param settings for storing user preferences.
 */
data class PlatformDependencies(
  val settings: () -> Settings,
  val sqlDriver: () -> SqlDriver
) {

  fun asKoinModule() = org.koin.dsl.module {
    single { sqlDriver() }
    single { settings() }
  }
}
