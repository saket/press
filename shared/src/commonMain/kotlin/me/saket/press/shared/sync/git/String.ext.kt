package me.saket.press.shared.sync.git

fun String.hasMultipleOf(character: Char): Boolean {
  return indexOfFirst { it == character } != indexOfLast { it == character }
}
