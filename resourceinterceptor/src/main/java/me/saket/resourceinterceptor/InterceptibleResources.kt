package me.saket.resourceinterceptor

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import java.util.Stack

private typealias ResourceId = Int

class InterceptibleResources(private val base: Resources) : ResourcesWrapper(base) {
  private val idsBeingIntercepted = Stack<ResourceId>()
  private val drawableInterceptors = mutableMapOf<ResourceId, DrawableInterceptor>()
  private val colorInterceptors = mutableMapOf<ResourceId, ColorInterceptor>()

  fun setInterceptor(@DrawableRes resId: ResourceId, interceptor: DrawableInterceptor) {
    drawableInterceptors[resId] = interceptor
  }

  fun setInterceptor(@ColorRes resId: ResourceId, interceptor: ColorInterceptor) {
    colorInterceptors[resId] = interceptor
  }

  fun removeInterceptor(resId: ResourceId) {
    drawableInterceptors.remove(resId)
  }

  override fun getDrawable(id: ResourceId, theme: Theme) = interceptOrGetDrawable(id, theme)
  override fun getDrawable(id: ResourceId) = interceptOrGetDrawable(id, theme = null)

  override fun getColor(id: ResourceId, theme: Theme?) = interceptOrGetColor(id, theme)
  override fun getColor(id: ResourceId) = interceptOrGetColor(id, theme = null)

  private fun interceptOrGetDrawable(@DrawableRes resId: ResourceId, theme: Theme?): Drawable? {
    val interceptor = drawableInterceptors[resId]
    var intercepted: Drawable? = null

    if (interceptor != null) {
      avoidInfiniteLoop(resId) {
        val systemDrawable = { baseDrawable(resId, theme)!! }
        intercepted = interceptor.intercept(systemDrawable)
      }
    }

    return intercepted ?: baseDrawable(resId, theme)
  }

  private fun baseDrawable(@DrawableRes resId: ResourceId, theme: Theme?): Drawable? =
    when (theme) {
      null -> base.getDrawable(resId)
      else -> base.getDrawable(resId, theme)
    }

  private fun interceptOrGetColor(@ColorRes resId: ResourceId, theme: Theme?): Int {
    val interceptor = colorInterceptors[resId]
    var intercepted: Int? = null

    if (interceptor != null) {
      avoidInfiniteLoop(resId) {
        val systemColor = { baseColor(resId, theme) }
        intercepted = interceptor.intercept(systemColor)
      }
    }

    return intercepted ?: baseColor(resId, theme)
  }

  private fun baseColor(@ColorRes resId: ResourceId, theme: Theme?): Int {
    return when (theme) {
      null -> base.getColor(resId)
      else -> base.getColor(resId, theme)
    }
  }

  private inline fun avoidInfiniteLoop(resId: ResourceId, crossinline block: () -> Unit) {
    if (resId in idsBeingIntercepted) {
      return
    }

    idsBeingIntercepted += resId
    block()
    idsBeingIntercepted -= resId
  }
}
