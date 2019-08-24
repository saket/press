package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.FencedCodeBlock
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

  private fun fencedCodeVisitor() =
    object : NodeVisitor<FencedCodeBlock> {
      override fun visit(
        node: FencedCodeBlock,
        pool: SpanPool,
        writer: SpanWriter
      ) {
        writer.add(pool.indentedCodeBlock(), node.startOffset, node.endOffset)
        writer.add(pool.monospaceTypeface(), node.startOffset, node.endOffset)
      }
    }
}