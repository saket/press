package me.saket.press.shared.di

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

// This exists because making components extend KoinComponent results in
// Dagger complaining about not being able to resolve koin related classes.
internal inline fun <reified T> koin(
  qualifier: Qualifier? = null,
  noinline parameters: ParametersDefinition? = null
): T {
  return GlobalContext.get().koin.get(parameters = parameters, qualifier = qualifier)
}
