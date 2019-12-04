package me.saket.press.shared.home

import me.saket.press.shared.di.koin
import me.saket.press.shared.navigation.Navigator
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

object SharedHomeComponent {

  val module = module {
    factory { (navigator: Navigator, includeEmptyNotes: Boolean) -> HomePresenter(get(), navigator, includeEmptyNotes) }
  }

  fun presenter(
    navigator: Navigator,
    includeEmptyNotes: Boolean
  ) = koin<HomePresenter> { parametersOf(navigator, includeEmptyNotes) }
}
