package me.saket.press.shared.keyboard

import me.saket.press.shared.di.koin
import org.koin.dsl.module

class SharedKeyboardComponent {
  val module = module {
    single<KeyboardShortcuts> { RealKeyboardShortcuts() }
  }

  companion object {
    fun shortcuts(): KeyboardShortcuts = koin()
  }
}
