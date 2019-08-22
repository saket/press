package me.saket.wysiwyg.spans

actual fun SpanPool.foregroundColor(color: Int): WysiwygSpan =
  get { ForegroundColorSpan(recycler) }.apply {
    this.color = color
  }

actual fun SpanPool.italics(): WysiwygSpan =
  get { StyleSpan(recycler) }.apply {
    style = StyleSpan.Style.ITALIC
  }