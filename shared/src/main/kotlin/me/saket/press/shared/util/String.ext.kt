package me.saket.press.shared.util

actual fun String.toLowerCase(locale: Locale): String {
  return toLowerCase(when (locale) {
    Locale.US -> java.util.Locale.US
  })
}
