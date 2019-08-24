package me.saket.wysiwyg.spans

import me.saket.wysiwyg.WysiwygTheme

actual data class ThematicBreakSpan actual constructor(
  val theme: WysiwygTheme,
  val recycler: Recycler,
  actual val syntax: CharSequence
) : WysiwygSpan {

  override fun recycle() {
    TODO()
  }
}