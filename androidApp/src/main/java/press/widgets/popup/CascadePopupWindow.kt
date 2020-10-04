package press.widgets.popup

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.util.TypedValue
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat

/**
 * Mimics [PopupMenu] by,
 * - offering the same entry & exit transitions
 * - dismissing on outside tap
 * - setting a default elevation
 */
@Suppress("LeakingThis")
abstract class CascadePopupWindow(
  private val context: Context
) : PopupWindow(context, null) {

  init {
    elevation = 16f.dip   // @dimen/floating_window_z
    isFocusable = true    // Dismiss on outside touch.
    isOutsideTouchable = true

    setBackgroundDrawable(null)   // Remove PopupWindow's default frame around the content.
    PopupWindowCompat.setOverlapAnchor(this, true)

    enterTransition = createEnterTransition()
    exitTransition = createExitTransition()
  }

  // Copies android's @transition/popup_window_enter
  private fun createEnterTransition(): Transition {
    return TransitionSet().apply {
      ordering = TransitionSet.ORDERING_TOGETHER
      addTransition(EpicenterTranslateClipReveal().also { it.duration = 250 })
      addTransition(Fade().also { it.duration = 100; })
    }
  }

  // Copies android's @transition/popup_window_exit
  private fun createExitTransition(): Transition {
    return Fade().also { it.duration = 300 }
  }

  protected val Float.dip: Float
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
    }

  protected val Int.dip: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
    }

  protected fun createRippleDrawable(color: Int): Drawable {
    val shape = PaintDrawable(Color.TRANSPARENT)
    val mask = PaintDrawable(Color.BLACK)
    return RippleDrawable(ColorStateList.valueOf(color), shape, mask)
  }
}
