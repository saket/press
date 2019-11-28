package me.saket.press.shared.di

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.Settings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

actual object SharedAppComponent : BaseSharedAppComponent() {

  fun initialize(appContext: Application) {
    val platformDependencies = module {
      single<Context> { appContext }
      single<SqlDriver> { androidSqliteDriver(appContext) }
      single<Settings> { androidSettings() }
    }
    setupGraph(platformDependencies)
  }

  private fun androidSqliteDriver(appContext: Application) =
    AndroidSqliteDriver(PressDatabase.Schema, appContext, "press.db")

  private fun Scope.androidSettings() =
    AndroidSettings(PreferenceManager.getDefaultSharedPreferences(get()))
}
