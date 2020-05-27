package me.saket.press.shared.di

import com.russhwolf.settings.AppleSettings
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import me.saket.press.PressDatabase
import me.saket.press.shared.sync.git.AppStorage
import platform.Foundation.NSUserDefaults

actual object SharedAppComponent : BaseSharedAppComponent() {

  fun initialize() {
    setupGraph(PlatformDependencies(
        sqlDriver = { nativeSqliteDriver() },
        settings = { appleSettings() },
        storage = { AppStorage(path = "todo") }
    ))
  }

  private fun nativeSqliteDriver() =
    NativeSqliteDriver(PressDatabase.Schema, "press.db")

  private fun appleSettings() =
    AppleSettings(NSUserDefaults.standardUserDefaults())
}
