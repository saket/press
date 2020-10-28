package press.widgets

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mikepenz.itemanimators.DefaultAnimator

@Suppress("FunctionName")
fun View.SlideDownItemAnimator() = SlideAlphaAnimator(
  animationTranslationX = 0f,
  animationTranslationY = dp(32f)
)

/**
 * - Plays fade and translate animations together, unlike the default animator.
 * - Sets the z-index of items being moved higher than items being removed they animates _over_ them.
 */
@Suppress("UNCHECKED_CAST")
class SlideAlphaAnimator(
  private val animationTranslationX: Float,
  private val animationTranslationY: Float
) : DefaultAnimator<Any>() {
  init {
    addDuration = 250
    removeDuration = 250
    moveDuration = 250
    withInterpolator(FastOutSlowInInterpolator())
  }

  override fun addAnimationPrepare(holder: ViewHolder) {
    holder.itemView.apply {
      translationX = -animationTranslationX
      translationY = -animationTranslationY
      alpha = 0f
    }
  }

  override fun addAnimation(holder: ViewHolder): ViewPropertyAnimatorCompat {
    return ViewCompat.animate(holder.itemView)
      .translationX(0f)
      .translationY(0f)
      .alpha(1f)
      .setDuration(moveDuration)
      .setInterpolator(interpolator)
  }

  override fun addAnimationCleanup(holder: ViewHolder) {
    holder.itemView.apply {
      translationX = 0f
      translationY = 0f
      translationZ = 0f
      alpha = 1f
    }
  }

  override fun getAddDelay(remove: Long, move: Long, change: Long): Long {
    return 0
  }

  override fun getRemoveDelay(remove: Long, move: Long, change: Long): Long {
    return 0
  }

  override fun resetAnimation(holder: ViewHolder) {
    super.resetAnimation(holder)
    holder.itemView.translationZ = 0f
  }

  override fun onMoveStarting(holder: ViewHolder) {
    super.onMoveStarting(holder)
    holder.itemView.translationZ = 1f
  }

  override fun onMoveFinished(holder: ViewHolder) {
    super.onMoveFinished(holder)
    holder.itemView.translationZ = 0f
  }

  override fun removeAnimation(holder: ViewHolder): ViewPropertyAnimatorCompat {
    holder.itemView.translationZ = 0f
    return ViewCompat.animate(holder.itemView)
      .setDuration(removeDuration)
      .alpha(0f)
      .translationX(-animationTranslationX)
      .translationY(-animationTranslationY)
      .setInterpolator(interpolator)
  }

  override fun removeAnimationCleanup(holder: ViewHolder) {
    holder.itemView.apply {
      translationX = 0f
      translationY = 0f
      alpha = 1f
      holder.itemView.translationZ = 0f
    }
  }
}
