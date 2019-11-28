package me.saket.wysiwyg.spans

import me.saket.wysiwyg.style.WysiwygStyle

actual data class ThematicBreakSpan actual constructor(
  val style: WysiwygStyle,
  val recycler: Recycler,
  actual val syntax: CharSequence
) : WysiwygSpan {

  override fun recycle() {
    TODO()
  }
}
