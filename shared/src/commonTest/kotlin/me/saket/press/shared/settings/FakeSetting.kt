package me.saket.press.shared.settings

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import me.saket.press.shared.util.FreezableAtomicReference

data class FakeSetting<T : Any>(val defaultValue: T?) : Setting<T> {
  private val reference = FreezableAtomicReference(defaultValue)

  override fun get(): T? = reference.value

  override fun set(value: T?) {
    this.reference.value = value
  }

  override fun listen(): Observable<T?> {
    return observableOf(reference.value)
  }
}
