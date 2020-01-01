package me.saket.wysiwyg.spans

/**
 * Used for identifying spans that should be reset on every text change.
 */
interface WysiwygSpan {
  fun recycle()
}
