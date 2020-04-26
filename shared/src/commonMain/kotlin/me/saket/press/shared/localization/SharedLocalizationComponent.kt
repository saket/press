package me.saket.press.shared.localization

import me.saket.press.shared.di.koin
import org.koin.dsl.module

class SharedLocalizationComponent {
  val module = module {
    single { ENGLISH_STRINGS }
  }

  companion object {
    fun strings(): Strings = koin()
  }
}
