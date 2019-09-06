package me.saket.compose.shared.di

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition

// This exists because making components extend KoinComponent results in
// Dagger complaining about not being able to resolve koin related classes.
internal inline fun <reified T> koin(
  noinline parameters: ParametersDefinition? = null
): T {
  return GlobalContext.get().koin.get(parameters = parameters)
}