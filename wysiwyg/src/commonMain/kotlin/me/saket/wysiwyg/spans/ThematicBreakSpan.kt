package me.saket.wysiwyg.spans

import me.saket.wysiwyg.WysiwygTheme
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType

expect class ThematicBreakSpan(
  theme: WysiwygTheme,
  recycler: Recycler,
  syntax: CharSequence,
  syntaxType: ThematicBreakSyntaxType
) : WysiwygSpan {
  val syntax: CharSequence
  val syntaxType: ThematicBreakSyntaxType
}