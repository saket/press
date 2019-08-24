package me.saket.wysiwyg.spans

import me.saket.wysiwyg.WysiwygTheme

expect class ThematicBreakSpan(
  theme: WysiwygTheme,
  recycler: Recycler,
  syntax: CharSequence
) : WysiwygSpan {

  /** Used for calculating the left offset to avoid drawing under the text. */
  val syntax: CharSequence
}