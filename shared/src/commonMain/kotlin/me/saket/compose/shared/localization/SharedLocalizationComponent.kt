package me.saket.compose.shared.localization

import me.saket.compose.shared.di.koin
import org.koin.dsl.module

object SharedLocalizationComponent {
  val module = module {
    single { ENGLISH_STRINGS }
  }

  fun strings(): Strings = koin()
}