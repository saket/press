package me.saket.wysiwyg.spans

// TODO: implement this span.
class ParagraphBreakSpan(private val recycler: Recycler) : WysiwygSpan {

  override fun recycle() {
    recycler(this)
  }
}
