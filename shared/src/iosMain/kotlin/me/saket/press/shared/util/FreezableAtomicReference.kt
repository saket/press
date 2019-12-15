package me.saket.press.shared.util

import kotlin.native.concurrent.FreezableAtomicReference
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

actual class FreezableAtomicReference<T> actual constructor(initialValue: T) {
  private val delegate = FreezableAtomicReference(initialValue)

  actual var value: T
    get() = delegate.value
    set(value) {
      if (isFrozen) {
        value.freeze()
      }
      delegate.value = value
    }
}
