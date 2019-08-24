package me.saket.wysiwyg.spans

import me.saket.wysiwyg.WysiwygTheme
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType

actual data class ThematicBreakSpan actual constructor(
  val theme: WysiwygTheme,
  val recycler: Recycler,
  actual val syntax: CharSequence,
  actual val syntaxType: ThematicBreakSyntaxType
) : WysiwygSpan {

  override fun recycle() {
    TODO()
  }
}