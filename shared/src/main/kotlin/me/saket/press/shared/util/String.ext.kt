@file:Suppress("NOTHING_TO_INLINE")

package me.saket.press.shared.util

import kotlin.text.format as kotlinFormat

actual inline fun String.toLowerCase(locale: Locale): String {
  return toLowerCase(
    when (locale) {
      Locale.US -> java.util.Locale.US
    }
  )
}

actual fun String.format(vararg args: Any): String =
  kotlinFormat(*args)
