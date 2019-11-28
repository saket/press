package me.saket.wysiwyg.spans

import me.saket.wysiwyg.style.WysiwygStyle

expect class ThematicBreakSpan(
  style: WysiwygStyle,
  recycler: Recycler,
  syntax: CharSequence
) : WysiwygSpan {

  /** Used for calculating the left offset to avoid drawing under the text. */
  val syntax: CharSequence
}
