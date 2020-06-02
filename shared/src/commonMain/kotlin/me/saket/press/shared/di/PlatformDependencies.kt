package me.saket.press.shared.di

import com.russhwolf.settings.Settings
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.shared.sync.git.DeviceInfo

/**
 * @param settings for storing user preferences.
 */
data class PlatformDependencies(
  val settings: () -> Settings,
  val sqlDriver: () -> SqlDriver,
  val deviceInfo: () -> DeviceInfo
) {

  fun asKoinModule() = org.koin.dsl.module {
    single { sqlDriver() }
    single { settings() }
    factory { deviceInfo() }
  }
}
