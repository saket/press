package me.saket.press.shared.localization

import me.saket.press.shared.di.koin
import org.koin.dsl.module

object SharedLocalizationComponent {
  val module = module {
    single { ENGLISH_STRINGS }
  }

  fun strings(): Strings = koin()
}