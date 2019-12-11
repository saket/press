package me.saket.press.shared.di

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.Settings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

actual object SharedAppComponent : BaseSharedAppComponent() {

  fun initialize(appContext: Application) {
    setupGraph(PlatformDependencies(
        sqlDriver = { androidSqliteDriver(appContext) },
        settings = { androidSettings(appContext) }
    ))
  }

  private fun androidSqliteDriver(appContext: Application) =
    AndroidSqliteDriver(PressDatabase.Schema, appContext, "press.db")

  private fun androidSettings(appContext: Application) =
    AndroidSettings(PreferenceManager.getDefaultSharedPreferences(appContext))
}
