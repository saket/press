package me.saket.compose.shared.di

import android.app.Application
import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import me.saket.compose.ComposeDatabase
import org.koin.dsl.module

actual object SharedAppComponent : BaseSharedAppComponent() {

  fun initialize(appContext: Application) {
    val platformDependencies = module {
      single<Context> { appContext }
      single<SqlDriver> {
        AndroidSqliteDriver(ComposeDatabase.Schema, appContext)
      }
    }
    setupGraph(platformDependencies)
  }
}
