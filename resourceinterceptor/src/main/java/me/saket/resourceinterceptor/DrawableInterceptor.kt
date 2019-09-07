package me.saket.resourceinterceptor

import android.graphics.drawable.Drawable

typealias SystemDrawable = () -> Drawable?

class DrawableInterceptor(val interceptor: (SystemDrawable) -> Drawable?) {
  operator fun invoke(systemDrawable: SystemDrawable) = interceptor(systemDrawable)
}