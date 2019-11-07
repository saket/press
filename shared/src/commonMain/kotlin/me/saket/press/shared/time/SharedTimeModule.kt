package me.saket.press.shared.time

import org.koin.dsl.module

internal object SharedTimeModule {
  val module = module {
    single<Clock> { RealClock() }
  }
}