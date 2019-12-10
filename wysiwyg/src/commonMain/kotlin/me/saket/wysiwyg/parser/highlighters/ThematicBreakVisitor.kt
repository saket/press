package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.ThematicBreak
import me.saket.wysiwyg.parser.node.chars
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class ThematicBreakVisitor : NodeVisitor<ThematicBreak> {

  override fun visit(
    node: ThematicBreak,
    writer: SpanWriter
  ) {
    writer.addForegroundColor(
        color = writer.style.syntaxColor,
        from = node.startOffset,
        to = node.endOffset
    )

    val thematicBreakSyntax = node.chars

    // Suppressing because this otherwise doesn't compile for Flexmark, where Kotlin native
    // thinks String cannot be compared to BasedSequence (which actually implements CharSequence).
    @Suppress("ReplaceCallWithBinaryOperator")
    val clashesWithBoldSyntax = FOUR_ASTERISKS_HORIZONTAL_RULE.equals(thematicBreakSyntax)
    if (clashesWithBoldSyntax) {
      return
    }

    // Flexmark (Android) maintains a mutable String, which isn't a good idea to cache.
    val immutableSyntax = thematicBreakSyntax.toString()
    writer.addThematicBreak(
        syntax = immutableSyntax,
        from = node.startOffset,
        to = node.endOffset
    )
  }

  companion object {
    private const val FOUR_ASTERISKS_HORIZONTAL_RULE = "****"
  }
}

