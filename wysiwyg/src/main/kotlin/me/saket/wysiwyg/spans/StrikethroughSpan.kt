package me.saket.wysiwyg.spans

class StrikethroughSpan(
  val recycler: Recycler
) : android.text.style.StrikethroughSpan(), WysiwygSpan {

  override fun recycle() {
    recycler(this)
  }
}
