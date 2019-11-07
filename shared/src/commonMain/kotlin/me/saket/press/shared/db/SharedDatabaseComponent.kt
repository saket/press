package me.saket.press.shared.db

import com.badoo.reaktive.scheduler.ioScheduler
import me.saket.press.PressDatabase
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedDatabaseComponent {
  val module = module {
    single { PressDatabase(get(), get()) }
    single { get<PressDatabase>().noteQueries }
    single(named("io")) { ioScheduler }
  }
}