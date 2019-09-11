package me.saket.compose.shared.localization

import me.saket.compose.shared.ENGLISH_STRINGS
import org.koin.dsl.module

object SharedLocalizationComponent {
  val module = module {
    single { ENGLISH_STRINGS }
  }
}