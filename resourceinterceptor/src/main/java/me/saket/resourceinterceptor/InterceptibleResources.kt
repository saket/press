package me.saket.resourceinterceptor

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import java.util.Stack

internal class InterceptibleResources(private val base: Resources) : ResourcesWrapper(base) {
  private val idsBeingIntercepted = Stack<Int>()
  internal val interceptors = mutableMapOf<Int, DrawableInterceptor>()

  override fun getDrawable(id: Int, theme: Theme) = interceptOrGetDrawable(id, theme)
  override fun getDrawable(id: Int) = interceptOrGetDrawable(id, theme = null)

  private fun interceptOrGetDrawable(@DrawableRes resId: Int, theme: Theme?): Drawable? {
    val interceptor = interceptors[resId]
    var intercepted: Drawable? = null

    if (interceptor != null) {
      avoidInfiniteLoop(resId) {
        val systemDrawable: () -> Drawable? = { baseDrawable(resId, theme) }
        intercepted = interceptor.intercept(systemDrawable)
      }
    }

    return intercepted ?: baseDrawable(resId, theme)
  }

  private fun baseDrawable(@DrawableRes resId: Int, theme: Theme?): Drawable? =
    when (theme) {
      null -> base.getDrawable(resId)
      else -> base.getDrawable(resId, theme)
    }

  private inline fun avoidInfiniteLoop(resId: Int, block: () -> Unit) {
    if (resId in idsBeingIntercepted) {
      return
    }

    idsBeingIntercepted += resId
    block()
    idsBeingIntercepted -= resId
  }
}