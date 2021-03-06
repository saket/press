package me.saket.wysiwyg.formatting

/**
 * Applies `>` markdown syntax to selected text or at the cursor position.
 */
object BlockQuoteSyntaxApplier : CompoundableParagraphSyntaxApplier(
  leftSyntax = '>',
  addSurroundingLineBreaks = true
)

/**
 * Applies `*` markdown syntax to selected text or at the cursor position.
 */
object EmphasisSyntaxApplier : InlineSymmetricMarkdownSyntaxApplier(syntax = "*")

/**
 * Applies `**` markdown syntax to selected text or at the cursor position.
 */
object StrongEmphasisSyntaxApplier : InlineSymmetricMarkdownSyntaxApplier(syntax = "**")

/**
 * Applies `~~` markdown syntax to selected text or at the cursor position.
 */
object StrikethroughSyntaxApplier : InlineSymmetricMarkdownSyntaxApplier(syntax = "~~")

/**
 * Applies `#` markdown syntax to selected text or at the cursor position.
 */
object HeadingSyntaxApplier : CompoundableParagraphSyntaxApplier(
  leftSyntax = '#',
  addSurroundingLineBreaks = false
)

/**
 * Applies [`] markdown syntax to selected text or at the cursor position.
 */
object InlineCodeSyntaxApplier : InlineSymmetricMarkdownSyntaxApplier(syntax = "`")
