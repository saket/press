package me.saket.wysiwyg.formatting

data class ReplaceTextWith(
  val replacement: CharSequence,
  val newSelection: TextSelection?
)
