package me.saket.wysiwyg.formatting

data class ApplyMarkdownSyntax(
  val newText: String,
  val newSelection: TextSelection
)
