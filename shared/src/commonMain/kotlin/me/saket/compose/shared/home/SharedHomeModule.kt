package me.saket.compose.shared.home

import me.saket.compose.shared.di.koin
import me.saket.compose.shared.navigation.Navigator
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

object SharedHomeModule {

  val homeModule = module {
    factory { (navigator: Navigator) -> HomePresenter(get(), navigator) }
  }

  fun presenter(navigator: Navigator): HomePresenter = koin { parametersOf(navigator) }
}