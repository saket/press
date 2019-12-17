package me.saket.wysiwyg.formatting

/**
 * Intended to be used by client apps with a text formatting toolbar.
 * See base classes that can be used for inserting markdown syntax in text.
 *
 * TODO: Link, Thematic break, Block quote, Heading.
 */
interface MarkdownSyntaxApplier {
  fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax
}

data class ApplyMarkdownSyntax(val newText: String, val newSelection: TextSelection)
