package me.saket.press.shared.util

enum class Locale { US }

/** Kotlin doesn't support passing in a locale to [String.toLowerCase] yet. */
expect fun String.toLowerCase(locale: Locale): String

expect inline fun Char.isLetterOrDigit(): Boolean
