package me.saket.press.shared.di

import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

// This exists because making components extend KoinComponent results in
// Dagger complaining about not being able to resolve koin related classes.
internal inline fun <reified T> koin(
  qualifier: Qualifier? = null,
  noinline parameters: ParametersDefinition? = null
): T {
  return KoinContextHandler.get().get(parameters = parameters, qualifier = qualifier)
}
