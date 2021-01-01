package me.saket.press.shared.di

import com.russhwolf.settings.AppleSettings
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import io.ktor.client.engine.cio.CIO
import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.File
import me.saket.press.shared.ui.Clipboard
import platform.Foundation.NSUserDefaults

actual object SharedComponent : BaseSharedComponent() {
  fun initialize() {
    setupGraph(
      PlatformDependencies(
        sqlDriver = { sqliteDriver(it) },
        settings = { settings() },
        deviceInfo = { deviceInfo() },
        httpEngine = { CIO.create() },
        clipboard = { stubClipboard() }
      )
    )
  }

  private fun sqliteDriver(schema: SqlDriver.Schema) =
    NativeSqliteDriver(schema, "press.db")

  private fun settings() =
    AppleSettings(NSUserDefaults.standardUserDefaults())

  private fun deviceInfo() = object : DeviceInfo {
    override val appStorage get() = File("todo")
    override fun deviceName() = TODO()
    override fun supportsSplitScreen(): Boolean = false
  }

  private fun stubClipboard() = object : Clipboard {
    override fun copyPlainText(text: String): Unit = TODO()
    override fun copyRichText(htmlText: String): Unit = TODO()
  }
}
