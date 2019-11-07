package press.animation

import android.animation.Animator
import android.animation.TimeInterpolator
import androidx.collection.ArrayMap

/**
 * Copied from Plaid.
 *
 * https://halfthought.wordpress.com/2014/11/07/reveal-transition/
 *
 * Interrupting Activity transitions can yield an OperationNotSupportedException when the
 * transition tries to pause the animator. Yikes! We can fix this by wrapping the Animator:
 */
class NoPauseAnimator(private val mAnimator: Animator) : Animator() {
  private val mListeners: ArrayMap<AnimatorListener, AnimatorListener> = ArrayMap()

  override fun addListener(listener: Animator.AnimatorListener) {
    val wrapper = AnimatorListenerWrapper(this, listener)
    if (!mListeners.containsKey(listener)) {
      mListeners.put(listener, wrapper)
      mAnimator.addListener(wrapper)
    }
  }

  override fun cancel() {
    mAnimator.cancel()
  }

  override fun end() {
    mAnimator.end()
  }

  override fun getDuration(): Long {
    return mAnimator.duration
  }

  override fun getInterpolator(): TimeInterpolator {
    return mAnimator.interpolator
  }

  override fun setInterpolator(timeInterpolator: TimeInterpolator) {
    mAnimator.interpolator = timeInterpolator
  }

  override fun getListeners(): ArrayList<Animator.AnimatorListener> {
    return ArrayList(mListeners.keys)
  }

  override fun getStartDelay(): Long {
    return mAnimator.startDelay
  }

  override fun setStartDelay(delayMS: Long) {
    mAnimator.startDelay = delayMS
  }

  override fun isPaused(): Boolean {
    return mAnimator.isPaused
  }

  override fun isRunning(): Boolean {
    return mAnimator.isRunning
  }

  override fun isStarted(): Boolean {
    return mAnimator.isStarted
  }

  /* We don't want to override pause or resume methods because we don't want them
   * to affect mAnimator.
  public void pause();
  public void resume();
  public void addPauseListener(AnimatorPauseListener listener);
  public void removePauseListener(AnimatorPauseListener listener);
  */

  override fun removeAllListeners() {
    mListeners.clear()
    mAnimator.removeAllListeners()
  }

  override fun removeListener(listener: Animator.AnimatorListener) {
    val wrapper = mListeners.get(listener)
    if (wrapper != null) {
      mListeners.remove(listener)
      mAnimator.removeListener(wrapper)
    }
  }

  override fun setDuration(durationMS: Long): Animator {
    mAnimator.duration = durationMS
    return this
  }

  override fun setTarget(target: Any?) {
    mAnimator.setTarget(target)
  }

  override fun setupEndValues() {
    mAnimator.setupEndValues()
  }

  override fun setupStartValues() {
    mAnimator.setupStartValues()
  }

  override fun start() {
    mAnimator.start()
  }
}

private class AnimatorListenerWrapper internal constructor(
  private val mAnimator: Animator,
  private val mListener: Animator.AnimatorListener
) : Animator.AnimatorListener {

  override fun onAnimationStart(animator: Animator) {
    mListener.onAnimationStart(mAnimator)
  }

  override fun onAnimationEnd(animator: Animator) {
    mListener.onAnimationEnd(mAnimator)
  }

  override fun onAnimationCancel(animator: Animator) {
    mListener.onAnimationCancel(mAnimator)
  }

  override fun onAnimationRepeat(animator: Animator) {
    mListener.onAnimationRepeat(mAnimator)
  }
}