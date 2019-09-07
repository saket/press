package me.saket.resourceinterceptor

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources

class ContextResourceWrapper(
  base: Context,
  private val newResources: Resources
) : ContextWrapper(base) {

  override fun getResources(): Resources = newResources
}
