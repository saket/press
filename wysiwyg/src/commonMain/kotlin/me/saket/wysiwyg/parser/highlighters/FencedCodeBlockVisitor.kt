package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.highlighters.DelimitedNodeVisitor.Companion.highlightClosingSyntax
import me.saket.wysiwyg.parser.highlighters.DelimitedNodeVisitor.Companion.highlightOpeningSyntax
import me.saket.wysiwyg.parser.node.FencedCodeBlock
import me.saket.wysiwyg.parser.node.closingMarker
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.openingMarker
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.indentedCodeBlock
import me.saket.wysiwyg.spans.monospaceTypeface

class FencedCodeBlockVisitor : SyntaxHighlighter<FencedCodeBlock> {

  override fun visitor(node: FencedCodeBlock): NodeVisitor<FencedCodeBlock>? {
    val clashesWithStrikethrough = node.openingMarker.contains('~')
    return when {
      clashesWithStrikethrough -> null
      else -> fencedCodeVisitor()
    }
  }

  // FYI compileKotlinMetadata task fails with an
  // error if the return type isn't explicitly specified.
  private fun fencedCodeVisitor(): NodeVisitor<FencedCodeBlock> =
    object : NodeVisitor<FencedCodeBlock> {
      override fun visit(
        node: FencedCodeBlock,
        pool: SpanPool,
        writer: RealSpanWriter
      ) {
        writer.add(pool.indentedCodeBlock(), node.startOffset, node.endOffset)
        writer.add(pool.monospaceTypeface(), node.startOffset, node.endOffset)

        highlightOpeningSyntax(node.openingMarker, node.startOffset, writer, pool)
        highlightClosingSyntax(node.closingMarker, node.endOffset, writer, pool)
      }
    }
}
