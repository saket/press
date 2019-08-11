package util

import com.squareup.contour.HeightOfAllowedContext
import com.squareup.contour.XInt
import com.squareup.contour.YInt
import com.squareup.contour.YResolver
import widgets.Attr

val Int.x: XInt
  get() = XInt(this)

val Int.y: YInt
  get() = YInt(this)

fun HeightOfAllowedContext.heightOf(
  attr: Attr
): YResolver {
  return heightOf { attr.asDimension().y }
}