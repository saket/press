package me.saket.press.shared.settings

import com.badoo.reaktive.subject.behavior.BehaviorSubject

data class FakeSetting<T : Any>(private val defaultValue: T?) : Setting<T> {
  private val value = BehaviorSubject(defaultValue)

  override fun get(): T? = value.value
  override fun listen() = value

  override fun set(value: T?) {
    this.value.onNext(value)
  }
}
