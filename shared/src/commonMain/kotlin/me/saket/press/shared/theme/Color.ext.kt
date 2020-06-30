package me.saket.press.shared.theme

/** Copied from Android. */
fun Int.blendWith(color2: Int, ratio: Float): Int {
  val inverseRatio = 1 - ratio
  val color1 = this

  val a: Float = alpha(color1) * inverseRatio + alpha(color2) * ratio
  val r: Float = red(color1) * inverseRatio + red(color2) * ratio
  val g: Float = green(color1) * inverseRatio + green(color2) * ratio
  val b: Float = blue(color1) * inverseRatio + blue(color2) * ratio
  return argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
}

private fun alpha(color: Int): Int = color ushr 24
private fun red(color: Int): Int = color shr 16 and 0xFF
private fun green(color: Int): Int = color shr 8 and 0xFF
private fun blue(color: Int): Int = color and 0xFF

private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
  return alpha shl 24 or (red shl 16) or (green shl 8) or blue
}
