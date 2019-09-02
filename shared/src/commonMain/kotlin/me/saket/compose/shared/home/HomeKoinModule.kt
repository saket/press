package me.saket.compose.shared.home

import me.saket.compose.shared.di.BaseModule
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

object HomeKoinModule : BaseModule() {

  init {
    loadKoinModules(module {
      factory { HomePresenter() }
    })
  }

  fun presenter(): HomePresenter = get()
}