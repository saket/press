package me.saket.press.shared.localization

import me.saket.press.shared.util.format

class PluralString(
  private val one: String,
  private val many: String
) {
  fun format(quantity: Int): String {
    val string = when (quantity) {
      1 -> one
      else -> many
    }
    return string.format(quantity.toString())
  }

  fun format(quantity: Double) = format(quantity.toInt())
}
