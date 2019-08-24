package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.WysiwygTheme
import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.ASTERISKS
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.HYPHENS
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.UNDERSCORES
import me.saket.wysiwyg.parser.node.ThematicBreak
import me.saket.wysiwyg.parser.node.chars
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.Recycler
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.ThematicBreakSpan
import me.saket.wysiwyg.spans.foregroundColor

class ThematicBreakVisitor : NodeVisitor<ThematicBreak> {

  private val thematicBreakSpansPool = ThematicSpanPool()

  override fun visit(
    node: ThematicBreak,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    writer.add(pool.foregroundColor(pool.theme.syntaxColor), node.startOffset, node.endOffset)

    val thematicBreakSyntax = node.chars

    // Suppressing because this otherwise doesn't compile for Flexmark, where Kotlin native
    // thinks String cannot be compared to BasedSequence (which actually implements CharSequence).
    @Suppress("ReplaceCallWithBinaryOperator")
    val clashesWithBoldSyntax = FOUR_ASTERISKS_HORIZONTAL_RULE.equals(thematicBreakSyntax)
    if (clashesWithBoldSyntax) {
      return
    }

    val syntaxType = when (thematicBreakSyntax[0]) {
      '*' -> ASTERISKS
      '-' -> HYPHENS
      '_' -> UNDERSCORES
      else -> throw UnsupportedOperationException(
          "Unknown thematic break mode: $thematicBreakSyntax"
      )
    }

    // Caching mutable BasedSequence isn't a good idea.
    val immutableThematicBreakChars = thematicBreakSyntax.toString()

    val hrSpan = thematicBreakSpansPool.get(
        theme = pool.theme,
        syntax = immutableThematicBreakChars,
        syntaxType = syntaxType
    )
    writer.add(hrSpan, node.startOffset, node.endOffset)
  }

  companion object {
    private const val FOUR_ASTERISKS_HORIZONTAL_RULE = "****"
  }
}

internal class ThematicSpanPool {
  private val pool = mutableMapOf<String, ThematicBreakSpan>()

  private val recycler: Recycler = { span ->
    require(span is ThematicBreakSpan)
    pool[recyclingKey(span)] = span
  }

  /**
   * @param syntax See [ThematicBreakSpan.syntax].
   */
  internal fun get(
    theme: WysiwygTheme,
    syntax: CharSequence,
    syntaxType: ThematicBreakSyntaxType
  ): ThematicBreakSpan {
    val key = recyclingKey(syntax, syntaxType)
    return pool.remove(key) ?: ThematicBreakSpan(theme, recycler, syntax, syntaxType)
  }

  private fun recyclingKey(span: ThematicBreakSpan) =
    recyclingKey(span.syntax, span.syntaxType)

  private fun recyclingKey(
    syntax: CharSequence,
    syntaxType: ThematicBreakSyntaxType
  ) =
    "${syntax}_$syntaxType"
}