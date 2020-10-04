package press.widgets.popup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper
import androidx.core.animation.doOnEnd
import androidx.core.graphics.translationMatrix
import androidx.core.graphics.withClip
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import me.saket.press.R

internal class HeightAnimatableViewFlipper(context: Context) : BaseExpandableFlipper(context) {
  fun showView(view: View) {
    if (childCount == 0) {
      super.addView(view)
      return
    }

    waitForOngoingAnimation {
      super.addView(view)

      setupFlipAnimation(goingForward = true)
      val prevView = getChildAt(displayedChild)
      displayedChild = indexOfChild(view)

      doOnLayout {
        animateHeight(
            from = prevView.height,
            to = view.height,
            onEnd = {
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
        it.interpolator = FastOutSlowInInterpolator()
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
}

/** Copied from [https://github.com/saket/InboxRecyclerView]. */
@Suppress("LeakingThis")
abstract class BaseExpandableFlipper(context: Context) : ViewFlipper(context) {
  protected val animationDuration = 1000L

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
    super.setBackgroundDrawable(background?.let(::DrawSkippableDrawable))
  }

  internal fun background() = background as? DrawSkippableDrawable

  override fun draw(canvas: Canvas) {
    background()?.let {
      it.skip = false
      it.setBounds(left, top, right, clippedDimens?.height() ?: bottom)
      it.draw(canvas)
    }

    background()?.skip = true
    super.draw(canvas)
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
