package me.saket.resourceinterceptor

import android.graphics.drawable.Drawable

interface DrawableInterceptor {
  fun intercept(systemDrawable: () -> Drawable?): Drawable
}
