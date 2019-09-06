package me.saket.compose.shared.db

import com.badoo.reaktive.scheduler.ioScheduler
import me.saket.compose.ComposeDatabase
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedDatabaseComponent {
  val module = module {
    single { ComposeDatabase(get(), get()) }
    single { get<ComposeDatabase>().noteQueries }
    single(named("io")) { ioScheduler }
  }
}