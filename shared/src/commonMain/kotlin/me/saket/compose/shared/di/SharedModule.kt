package me.saket.compose.shared.di

import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.note.NoteKoinModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.parameter.ParametersDefinition

internal inline fun <reified T> koin(
  noinline parameters: ParametersDefinition? = null
): T {
  Initializer.initialize()
  return GlobalContext.get().koin.get(parameters = parameters)
}

internal object Initializer {
  private var initialized = false

  fun initialize() {
    if (initialized) return
    initialized = true

    startKoin {
      modules(listOf(
          HomeKoinModule.homeModule,
          NoteKoinModule.module
      ))
    }
  }
}