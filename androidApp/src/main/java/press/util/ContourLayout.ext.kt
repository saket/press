package press.util

import com.squareup.contour.HasYPositionWithoutHeight
import com.squareup.contour.XInt
import com.squareup.contour.YInt
import com.squareup.contour.solvers.YAxisSolver
import press.widgets.Attr

val Int.x: XInt
  get() = XInt(this)

val Int.y: YInt
  get() = YInt(this)

fun HasYPositionWithoutHeight.heightOf(
  attr: Attr
): YAxisSolver {
  return heightOf { attr.asDimension().y }
}
