package me.saket.compose.shared.home

import me.saket.compose.shared.di.koin
import org.koin.dsl.module

object HomeKoinModule {

  val homeModule = module {
    factory { HomePresenter(get()) }
  }

  fun presenter(): HomePresenter = koin()
}