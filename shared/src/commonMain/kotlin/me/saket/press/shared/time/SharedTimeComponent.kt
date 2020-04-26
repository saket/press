package me.saket.press.shared.time

import org.koin.dsl.module

internal class SharedTimeComponent {
  val module = module {
    single<Clock> { RealClock() }
  }
}
