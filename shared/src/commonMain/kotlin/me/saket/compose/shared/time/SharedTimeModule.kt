package me.saket.compose.shared.time

import org.koin.dsl.module

internal object SharedTimeModule {
  val module = module {
    single<Clock> { RealClock() }
  }
}