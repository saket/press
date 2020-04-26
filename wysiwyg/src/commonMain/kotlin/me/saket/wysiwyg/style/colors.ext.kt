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
