package me.saket.press.shared.localization

import android.content.Context
import me.saket.press.shared.SharedAppComponent

/**
 * Keeping this an extension function over Context to keep it consistent with context.getString().
 */
@Suppress("unused")
fun Context.strings(): Strings {
  return SharedAppComponent.strings()
}
