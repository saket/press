package me.saket.resourceinterceptor

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import java.util.Stack

abstract class ResourceInterceptibleContext(base: Context) : ContextWrapper(base) {

  private val idsBeingIntercepted = Stack<Int>()

  private val interceptibleResources = object : ResourcesWrapper(super.getResources()) {
    override fun getDrawable(id: Int, theme: Theme): Drawable? {
      return when (id) {
        in idsBeingIntercepted -> super.getDrawable(id, theme)
        else -> interceptDrawable(id, theme)
      }
    }
    override fun getDrawable(id: Int): Drawable? {
      return when (id) {
        in idsBeingIntercepted -> super.getDrawable(id)
        else -> interceptDrawable(id, theme = null)
      }
    }
  }

  open fun interceptDrawable(@DrawableRes resId: Int, theme: Theme?): Drawable? {
    return avoidInfiniteLoop(resId) {
      when (theme) {
        null -> interceptibleResources.getDrawable(resId)
        else -> interceptibleResources.getDrawable(resId, theme)
      }
    }
  }

  private inline fun <T> avoidInfiniteLoop(resId: Int, crossinline block: () -> T): T {
    idsBeingIntercepted += resId
    val value = block()
    idsBeingIntercepted -= resId
    return value
  }

  override fun getResources(): Resources = interceptibleResources
}