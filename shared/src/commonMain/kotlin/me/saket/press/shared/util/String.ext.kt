package me.saket.press.shared.util

enum class Locale { US }

/** Kotlin doesn't support passing in a locale to [String.toLowerCase] yet. */
expect fun String.toLowerCase(locale: Locale): String

fun Char.isDigit(): Boolean = this in '0'..'9'

fun Char.isLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

fun Char.isLetterOrDigit(): Boolean = isLetter() || isDigit()

expect fun String.format(vararg args: Any): String
