package me.saket.press.shared.util

actual class FreezableAtomicReference<T> actual constructor(initialValue: T) {
  actual var value: T = initialValue
}
