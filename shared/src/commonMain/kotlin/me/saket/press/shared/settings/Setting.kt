package me.saket.press.shared.settings

import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable
import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.set

/**
 * Inspired from RxPreferences, a `Setting` abstracts a single row in the key/value
 * store, letting usages fetch and save its value without requiring to know about
 * the setting key.
 *
 * todo: move to /preferences
 */
@OptIn(ExperimentalListener::class)
interface Setting<T : Any> {
  fun get(): T?
  fun set(value: T?)
  fun listen(): Observable<T?>

  companion object {
    fun <T : Any> create(
      settings: ObservableSettings,
      key: String,
      from: (String) -> T,
      to: (T) -> String,
      defaultValue: T?
    ): Setting<T> {
      return object : Setting<T> {
        override fun get(): T? {
          val saved = settings.getStringOrNull(key)
          return if (saved != null) from(saved) else defaultValue
        }

        override fun set(value: T?) {
          settings[key] = if (value != null) to(value) else null
        }

        override fun listen(): Observable<T?> {
          return observable { emitter ->
            val listener = settings.addListener(key) { emitter.onNext(get()) }
            emitter.setCancellable { listener.deactivate() }
            emitter.onNext(get()) // initial value.
          }
        }
      }
    }
  }
}
