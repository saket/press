package me.saket.press.shared.home

import me.saket.press.shared.di.koin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

object SharedHomeComponent {

  val module = module {
    factory { (args: HomePresenter.Args) -> HomePresenter(args, get()) }
  }

  fun presenter(args: HomePresenter.Args) = koin<HomePresenter> { parametersOf(args) }
}
