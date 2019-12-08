package me.saket.wysiwyg.spans

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View

class ClickableUrlSpan(
  val recycler: Recycler
) : URLSpan("placeholder_url"), WysiwygSpan {

  lateinit var url: String

  override fun getURL() = url

  override fun updateDrawState(ds: TextPaint) {
    // No-op.
  }

  override fun recycle() {
    recycler(this)
  }
}
