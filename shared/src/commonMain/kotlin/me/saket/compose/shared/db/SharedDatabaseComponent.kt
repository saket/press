package me.saket.compose.shared.db

import me.saket.compose.ComposeDatabase
import org.koin.dsl.module

internal object SharedDatabaseComponent {
  val module = module {
    single { ComposeDatabase(get(), get()) }
  }
}