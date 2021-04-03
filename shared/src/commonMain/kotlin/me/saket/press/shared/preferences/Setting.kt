package me.saket.press.shared.preferences

import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.set

/**
 * Inspired from RxPreferences, a `Setting` abstracts a single row in the key/value
 * store, letting usages fetch and save its value without requiring to know about
 * the setting key.
 */
@OptIn(ExperimentalSettingsApi::class)
abstract class Setting<T : Any> {
  abstract fun get(): T?
  abstract fun set(value: T?)
  internal abstract fun listen(): Observable<T?>
}

fun <T : Any> ObservableSettings.setting(
  key: String,
  from: (String) -> T,
  to: (T) -> String,
  defaultValue: T?
): Setting<T> {
  val settings = this
  return object : Setting<T>() {
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
