package press.widgets.popup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ViewFlipper
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withClip
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import me.saket.press.R

@Suppress("NAME_SHADOWING")
internal class HeightAnimatableViewFlipper(context: Context) : BaseHeightClippableFlipper(context) {

  override fun addView(child: View, index: Int, params: android.view.ViewGroup.LayoutParams) {
    if (childCount == 0) {
      super.addView(child, index, params)
      return
    }

    // Queue children so that they show up one-by-one.
    waitForOngoingAnimation {
      super.addView(child, index, params)

      setupFlipAnimation(goingForward = true)
      val prevView = getChildAt(displayedChild)
      displayedChild = indexOfChild(child)

      doOnLayout {
        animateHeight(
            from = prevView.height,
            to = child.height,
            onEnd = {
              // ViewFlipper plays animation if the view
              // count goes down, which isn't wanted here.
              inAnimation = null
              outAnimation = null
              removeView(prevView)
            }
        )
      }
    }
  }

  private fun waitForOngoingAnimation(action: () -> Unit) {
    if (!animator.isRunning) action()
    else animator.doOnEnd { action() }
  }

  override fun onDetachedFromWindow() {
    animator.cancel()
    super.onDetachedFromWindow()
  }

  private fun setupFlipAnimation(goingForward: Boolean) {
    val inflate = { animRes: Int ->
      AnimationUtils.loadAnimation(context, animRes).also {
        it.duration = animationDuration
        it.interpolator = animationInterpolator
      }
    }

    if (goingForward) {
      inAnimation = inflate(R.anim.cascademenu_submenu_enter)
      outAnimation = inflate(R.anim.cascademenu_mainmenu_exit)
    } else {
      inAnimation = inflate(R.anim.cascademenu_mainmenu_enter)
      outAnimation = inflate(R.anim.cascademenu_submenu_exit)
    }
  }

  private val matrixReader = MatrixReader()
  override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
    val default = { super.drawChild(canvas, child, drawingTime) }
    val incomingView = getChildAt(indexOfChild(child) + 1) ?: return default()
    val incomingMatrix = incomingView.animationMatrix ?: return default()

    // Incoming view may not have any background, resulting in cross-drawing of views
    // during animation. Clip the outgoing view to make it appear as if the incoming
    // view had an opaque background.
    val incomingX = incomingView.x + matrixReader.translationX(incomingMatrix)
    canvas.withClip(left, top, incomingX.toInt(), bottom) {
      return super.drawChild(canvas, child, drawingTime)
    }
    error("unreachable")
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // Outgoing Views don't receive touch events.
    val displayed: View? = getChildAt(displayedChild)
    return displayed?.dispatchTouchEvent(ev) ?: super.dispatchTouchEvent(ev)
  }
}

private class MatrixReader {
  private val matrixBuffer = FloatArray(9)

  fun translationX(matrix: Matrix): Float {
    matrix.getValues(matrixBuffer)
    return matrixBuffer[Matrix.MTRANS_X]
  }
}

@Suppress("LeakingThis")
abstract class BaseHeightClippableFlipper(context: Context) : ViewFlipper(context) {
  protected var animationDuration = 350L
  protected var animationInterpolator = FastOutSlowInInterpolator()

  // Because ViewGroup#getClipBounds creates a new Rect everytime.
  private var clippedDimens: Rect? = null
  protected var animator: ValueAnimator = ObjectAnimator()

  init {
    setWillNotDraw(false)
    outlineProvider = ViewOutlineProvider.BACKGROUND
  }

  override fun onDetachedFromWindow() {
    animator.cancel()
    super.onDetachedFromWindow()
  }

  protected fun animateHeight(from: Int, to: Int, onEnd: () -> Unit) {
    animator.cancel()

    animator = ObjectAnimator.ofFloat(0f, 1f).apply {
      duration = animationDuration
      interpolator = FastOutSlowInInterpolator()

      addUpdateListener {
        val scale = it.animatedValue as Float
        val newHeight = ((to - from) * scale + from).toInt()
        setClippedHeight(newHeight)
      }
      doOnEnd { onEnd() }
      start()
    }
  }

  @Suppress("DEPRECATION")
  override fun setBackgroundDrawable(background: Drawable?) {
    super.setBackgroundDrawable(background?.let(::DrawSuppressibleDrawable))
  }

  override fun draw(canvas: Canvas) {
    // Draw the background manually with clipped bounds, because
    // super.draw() will always reset it to View's bounds.
    background?.let {
      it.setBounds(left, top, right, clippedDimens?.height() ?: bottom)
      it.draw(canvas)
    }

    (background as DrawSuppressibleDrawable?).withDrawSuppressed {
      super.draw(canvas)
    }
  }

  private fun setClippedHeight(newHeight: Int) {
    clippedDimens = (clippedDimens ?: Rect()).also {
      it.set(left, top, right, newHeight)
    }
    clipBounds = clippedDimens

    invalidate()
    invalidateOutline()
  }
}
