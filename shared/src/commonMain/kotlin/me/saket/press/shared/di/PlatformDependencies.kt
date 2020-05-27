package me.saket.press.shared.di

import com.russhwolf.settings.Settings
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.shared.sync.git.AppStorage

/**
 * @param settings for storing user preferences.
 */
data class PlatformDependencies(
  val settings: () -> Settings,
  val sqlDriver: () -> SqlDriver,
  val storage: () -> AppStorage
) {

  fun asKoinModule() = org.koin.dsl.module {
    single { sqlDriver() }
    single { settings() }
    factory { storage() }
  }
}
