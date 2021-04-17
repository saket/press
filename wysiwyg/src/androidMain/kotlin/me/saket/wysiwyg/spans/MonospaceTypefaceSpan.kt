package me.saket.wysiwyg.spans

import android.text.style.TypefaceSpan

class MonospaceTypefaceSpan(val recycler: Recycler) : TypefaceSpan("monospace"), WysiwygSpan {

  override fun recycle() {
    recycler(this)
  }
}
