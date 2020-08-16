package me.saket.press.shared.util

/**
 * FreezableAtomicReference on native platforms, simple variable on JVM.
 * TODO: can stately be used instead?
 */
expect class FreezableAtomicReference<T>(initialValue: T) {
  var value: T
}
