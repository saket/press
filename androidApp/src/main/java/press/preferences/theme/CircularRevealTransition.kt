package press.preferences.theme

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Path.Direction.CCW
import android.graphics.Picture
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnPreDraw
import press.navigation.TheActivity
import kotlin.math.hypot
import kotlin.math.max

class CircularRevealTransition {
  private var animator = ValueAnimator()
  val isOngoing get() = animator.isRunning

  /**
   * @param anchor The anchor has two purposes:
   *
   * 1. It acts as the epicenter of the circular transition.
   *
   * 2. It cuts through the transition overlay so that any unfinished
   *    ripple animation is still visible, and doesn't end abruptly.
   */
  @Suppress("NAME_SHADOWING")
  fun beginTransition(sceneRoot: ViewGroup, anchor: View?) {
    // The overlay is applied to the Window's entire content
    // (decor View) to include the status and nav bars.
    val sceneRoot = sceneRoot.rootView as ViewGroup

    val sceneImage = sceneRoot.captureImage()
    sceneRoot.overlay.add(sceneImage)

    animator = sceneImage.createRevealAnimation(anchor).apply {
      duration = 400
      interpolator = PathInterpolator(0f, 0f, 0.5f, 1f)
    }

    // Recreate the view hierarchy so that the new theme is picked up.
    TheActivity.viewRecreateRequests.onNext(Unit)

    sceneRoot.doOnPreDraw {
      animator.start()
    }
    animator.doOnEnd {
      sceneRoot.overlay.remove(sceneImage)
    }
  }

  private fun View.captureImage(): RevealView {
    val view = this
    check(view.isLaidOut)

    val viewImage = Picture().let {
      val canvas = it.beginRecording(view.width, view.height)
      view.draw(canvas)
      it.endRecording()
      PictureDrawable(it)
    }

    return RevealView(context).also {
      it.layout(left, top, right, bottom)
      it.background = viewImage
    }
  }
}

/**
 * Unlike [ViewAnimationUtils.createCircularReveal], this clips *out* an (animating)
 * circle instead of clipping everything outside the circle.
 */
private class RevealView(context: Context) : View(context) {
  private val path = Path()

  override fun draw(canvas: Canvas) {
    val checkpoint = canvas.save()
    canvas.clipOutPath(path)
    super.draw(canvas)
    canvas.restoreToCount(checkpoint)
  }

  fun createRevealAnimation(anchor: View?): ValueAnimator {
    val anchorBounds: RectF
    val anchorCornerRadius: Float
    val epicenter: PointF

    if (anchor == null) {
      anchorBounds = RectF(0f, 0f, 0f, 0f)
      anchorCornerRadius = 0f
      epicenter = PointF(0f, 0f)

    } else {
      anchorBounds = anchor.locationInWindow()
      anchorCornerRadius = anchor.background.outline().radius
      check(anchorCornerRadius >= 0f) { "Non-rectangle anchors aren't supported yet." }
      epicenter = PointF(anchorBounds.centerX(), anchorBounds.centerY())
    }

    val startRadius = 0f
    val endRadius = fullyRevealedRadius(epicenter)

    return ObjectAnimator.ofFloat(startRadius, endRadius).apply {
      addUpdateListener {
        val radius = it.animatedValue as Float
        path.rewind()
        path.addOval(
          epicenter.x - radius,
          epicenter.y - radius,
          epicenter.x + radius,
          epicenter.y + radius,
          CCW
        )
        path.addRoundRect(anchorBounds, anchorCornerRadius, anchorCornerRadius, CCW)
        invalidate()
      }
    }
  }

  private fun fullyRevealedRadius(center: PointF): Float {
    return hypot(
      max(center.x, this.width - center.x).toDouble(),
      max(center.y, this.height - center.y).toDouble()
    ).toFloat()
  }

  private fun View.locationInWindow(): RectF {
    val loc = IntArray(2)
    getLocationInWindow(loc)
    return RectF(loc[0].toFloat(), loc[1].toFloat(), loc[0] + width.toFloat(), loc[1] + height.toFloat())
  }

  private fun Drawable.outline(): Outline {
    return Outline().also { getOutline(it) }
  }
}
