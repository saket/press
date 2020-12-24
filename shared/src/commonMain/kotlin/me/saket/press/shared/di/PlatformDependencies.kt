package me.saket.press.shared.di

import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import com.squareup.sqldelight.db.SqlDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import me.saket.press.PressDatabase
import me.saket.press.shared.db.createPressDatabase
import me.saket.press.shared.note.PrePopulatedNotes
import me.saket.press.shared.sync.git.DeviceInfo
import org.koin.core.scope.Scope

/**
 * @param settings for storing user preferences.
 */
@OptIn(ExperimentalListener::class)
data class PlatformDependencies(
  val settings: () -> ObservableSettings,
  val sqlDriver: (SqlDriver.Schema) -> SqlDriver,
  val deviceInfo: () -> DeviceInfo,
  val httpEngine: () -> HttpClientEngine
) {

  fun asKoinModule() = org.koin.dsl.module {
    single { sqlDriver(SeededSchema(this)) }
    single { settings() }
    factory { deviceInfo() }
    factory { httpEngine() }
  }

  private class SeededSchema(private val koin: Scope) : SqlDriver.Schema by PressDatabase.Schema {
    override fun create(driver: SqlDriver) {
      PressDatabase.Schema.create(driver)

      PrePopulatedNotes.seed(
        database = createPressDatabase(driver, json = koin.get()),
        clock = koin.get()
      )
    }
  }
}
