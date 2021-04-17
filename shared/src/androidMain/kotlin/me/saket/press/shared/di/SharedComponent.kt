package me.saket.press.shared.di

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager
import com.russhwolf.settings.AndroidSettings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.ktor.client.engine.okhttp.OkHttp
import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.File
import me.saket.press.shared.ui.RealClipboard

actual object SharedComponent : BaseSharedComponent() {
  fun initialize(appContext: Application) {
    setupGraph(
      PlatformDependencies(
        sqlDriver = { sqliteDriver(it, appContext) },
        settings = { settings(appContext) },
        deviceInfo = { deviceInfo(appContext) },
        httpEngine = { okHttpEngine() },
        clipboard = { RealClipboard(appContext) }
      )
    )
  }

  private fun sqliteDriver(schema: SqlDriver.Schema, appContext: Application) =
    AndroidSqliteDriver(schema, appContext, "press.db")

  private fun settings(appContext: Application) =
    AndroidSettings(PreferenceManager.getDefaultSharedPreferences(appContext))

  private fun deviceInfo(context: Context): DeviceInfo {
    return object : DeviceInfo {
      override val appStorage get() = File(context.filesDir.path)

      override fun deviceName(): String {
        // https://stackoverflow.com/a/45696806/2511884
        val bluetoothName = Settings.Secure.getString(context.contentResolver, "bluetooth_name")
        return bluetoothName ?: android.os.Build.MODEL
      }

      override fun supportsSplitScreen(): Boolean {
        // TODO: It may be a better idea to check the display
        //  size instead of default to true for all devices.
        return true
      }
    }
  }

  private fun okHttpEngine() = OkHttp.create {
    config {
      // Defaults to true by OkHttp but Ktor sets it to false.
      retryOnConnectionFailure(true)
    }
  }
}
