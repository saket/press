package me.saket.resourceinterceptor

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.annotation.DrawableRes

class ResourceInterceptibleContext(base: Context) : ContextWrapper(base) {
  private val interceptibleResources = InterceptibleResources(super.getResources())

  override fun getResources(): Resources = interceptibleResources

  fun setInterceptor(@DrawableRes resId: Int, interceptor: DrawableInterceptor) {
    interceptibleResources.interceptors[resId] = interceptor
  }

  fun removeInterceptor(@DrawableRes resId: Int) {
    interceptibleResources.interceptors.remove(resId)
  }
}
