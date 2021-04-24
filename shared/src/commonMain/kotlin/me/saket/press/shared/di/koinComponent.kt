package me.saket.press.shared.di

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools

// This exists because making components extend KoinComponent prevents Dagger
// (in :androidApp) from being able to resolve koin classes that is not in its classpath.
internal inline fun <reified T> koin(
  qualifier: Qualifier? = null,
  noinline parameters: ParametersDefinition? = null
): T {
  val koin = KoinPlatformTools.defaultContext().get()
  return koin.get(parameters = parameters, qualifier = qualifier)
}
