package me.saket.press.shared.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Inspired from RxPreferences, a `Setting` abstracts a single row in the key/value
 * store, letting usages fetch and save its value without requiring to know about
 * the setting key.
 */
interface Setting<T> {
  fun get(): T
  fun set(value: T)
}

fun <T> customTypeSetting(
  settings: Settings,
  key: String,
  from: (String) -> T,
  to: (T) -> String,
  defaultValue: T
): Setting<T> {
  return object : Setting<T> {
    override fun get(): T {
      val saved = settings.getStringOrNull(key)
      return if (saved != null) from(saved) else defaultValue
    }

    override fun set(value: T) {
      settings[key] = to(value)
    }
  }
}
