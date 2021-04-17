package me.saket.wysiwyg.util

import kotlin.text.isDigit as isKotlinDigit

actual fun Char.isDigit(): Boolean {
  return isKotlinDigit()
}
