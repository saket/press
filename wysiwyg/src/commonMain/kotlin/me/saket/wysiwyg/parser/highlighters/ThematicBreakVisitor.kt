package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.ThematicBreak
import me.saket.wysiwyg.parser.node.chars
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor
import me.saket.wysiwyg.spans.thematicBreak

class ThematicBreakVisitor : NodeVisitor<ThematicBreak> {

  override fun visit(
    node: ThematicBreak,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    writer.add(pool.foregroundColor(pool.style.syntaxColor), node.startOffset, node.endOffset)

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
    writer.add(pool.thematicBreak(immutableSyntax), node.startOffset, node.endOffset)
  }

  companion object {
    private const val FOUR_ASTERISKS_HORIZONTAL_RULE = "****"
  }
}

