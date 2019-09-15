package me.saket.resourceinterceptor

import android.graphics.drawable.Drawable

typealias SystemDrawable = () -> Drawable

interface DrawableInterceptor {
  fun intercept(systemDrawable: SystemDrawable): Drawable?
}

fun DrawableInterceptor(interceptor: (SystemDrawable) -> Drawable?): DrawableInterceptor {
  return object : DrawableInterceptor {
    override fun intercept(systemDrawable: SystemDrawable) = interceptor(systemDrawable)
  }
}