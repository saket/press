package me.saket.press.shared.localization

import android.content.Context

/**
 * Keeping this an extension function over Context to keep it with context.getString().
 */
fun Context.strings(): Strings {
  return SharedLocalizationComponent.strings()
}
