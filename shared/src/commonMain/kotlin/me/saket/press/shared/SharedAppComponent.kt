package me.saket.press.shared

import me.saket.press.shared.di.koin
import me.saket.press.shared.keyboard.KeyboardShortcuts
import me.saket.press.shared.keyboard.RealKeyboardShortcuts
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.time.Clock
import me.saket.press.shared.time.RealClock
import org.koin.dsl.module

class SharedAppComponent {
  val module = module {
    single<KeyboardShortcuts> { RealKeyboardShortcuts() }
    single { ENGLISH_STRINGS }
    single<Clock> { RealClock() }
  }

  companion object {
    fun strings(): Strings = koin()
    fun keyboardShortcuts(): KeyboardShortcuts = koin()
  }
}
