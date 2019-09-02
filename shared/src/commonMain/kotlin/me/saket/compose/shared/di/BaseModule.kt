package me.saket.compose.shared.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

abstract class BaseModule {

  private object Initializer {
    private var initialized = false

    fun initialize() {
      if (initialized) return
      initialized = true
      startKoin {}
    }
  }

  init {
    Initializer.initialize()
  }

  protected inline fun <reified T> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
  ): T = GlobalContext.get().koin.get(qualifier, parameters)
}