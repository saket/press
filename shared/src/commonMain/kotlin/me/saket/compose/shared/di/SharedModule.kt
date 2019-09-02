package me.saket.compose.shared.di

import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.note.NoteKoinModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

internal inline fun <reified T> koin(): T {
  Initializer.initialize()
  return GlobalContext.get().koin.get()
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