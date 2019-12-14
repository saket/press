package me.saket.press.shared.settings

import me.saket.press.shared.util.FreezableAtomicReference

data class FakeSetting<T>(val defaultValue: T) : Setting<T> {
  private val reference = FreezableAtomicReference(defaultValue)

  override fun get(): T = reference.value

  override fun set(value: T) {
    this.reference.value = value
  }
}
