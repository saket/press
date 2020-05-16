package me.saket.press.shared.home

import me.saket.press.shared.di.koin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SharedHomeComponent {

  val module = module {
    factory { (args: HomePresenter.Args) -> HomePresenter(args, get(), get()) }
  }

  companion object {
    fun presenter(args: HomePresenter.Args) = koin<HomePresenter> { parametersOf(args) }
  }
}
