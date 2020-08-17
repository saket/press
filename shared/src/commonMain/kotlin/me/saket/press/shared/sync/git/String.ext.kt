package me.saket.press.shared.sync.git

fun String.hasMultipleOf(character: Char): Boolean {
  return count { it == character } > 1
}
