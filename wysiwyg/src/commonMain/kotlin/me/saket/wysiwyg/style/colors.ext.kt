package me.saket.wysiwyg.style

/**
 * Copied from Android's Color.parseColor().
 */
fun String.parseColor(): Int {
  require(this[0] == '#') {
    throw IllegalArgumentException("Not a hex color: $this")
  }
  val color: Long = this.substring(1).toLong(16)
  return when (this.length) {
    // Use the default alpha (full opacity) if the color doesn't use one.
    7 -> (color or -0x1000000).toInt()
    9 -> color.toInt()
    else -> throw IllegalArgumentException("Unknown color: $this")
  }
}

fun Int.withOpacity(opacity: Float): Int {
  val transparent = 0x00FFFFFF
  val alpha = alpha() * opacity + transparent.alpha() * (1 - opacity)
  return argb(
    alpha = alpha.toInt(),
    red = red(),
    green = green(),
    blue = blue()
  )
}

private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
  return alpha shl 24 or (red shl 16) or (green shl 8) or blue
}

/** Alpha component of a color int. */
private fun Int.alpha(): Int = this ushr 24

/** Red component of a color int. */
private fun Int.red(): Int = this shr 16 and 0xFF

/** Green component of a color int. */
private fun Int.green(): Int = this shr 8 and 0xFF

/** Blue component of a color int. */
private fun Int.blue(): Int = this and 0xFF
