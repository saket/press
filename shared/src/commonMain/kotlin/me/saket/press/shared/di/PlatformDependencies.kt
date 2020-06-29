package me.saket.press.shared.di

import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.shared.sync.git.DeviceInfo

/**
 * @param settings for storing user preferences.
 */
@OptIn(ExperimentalListener::class)
data class PlatformDependencies constructor(
  val settings: () -> ObservableSettings,
  val sqlDriver: () -> SqlDriver,
  val deviceInfo: () -> DeviceInfo
) {

  fun asKoinModule() = org.koin.dsl.module {
    single { sqlDriver() }
    single { settings() }
    factory { deviceInfo() }
  }
}
