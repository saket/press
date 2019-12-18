package me.saket.wysiwyg.formatting

/**
 * Applies `#` markdown syntax to selected text or at the cursor position.
 */
object HeadingSyntaxApplier : CompoundableParagraphSyntaxApplier(leftSyntax = '#')
