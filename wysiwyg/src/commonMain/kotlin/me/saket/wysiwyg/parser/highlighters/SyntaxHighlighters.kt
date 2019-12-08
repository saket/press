package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.node.BlockQuote
import me.saket.wysiwyg.parser.node.BulletList
import me.saket.wysiwyg.parser.node.BulletListItem
import me.saket.wysiwyg.parser.node.Code
import me.saket.wysiwyg.parser.node.Emphasis
import me.saket.wysiwyg.parser.node.FencedCodeBlock
import me.saket.wysiwyg.parser.node.Heading
import me.saket.wysiwyg.parser.node.IndentedCodeBlock
import me.saket.wysiwyg.parser.node.LinkWithTitle
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.OrderedList
import me.saket.wysiwyg.parser.node.OrderedListItem
import me.saket.wysiwyg.parser.node.Strikethrough
import me.saket.wysiwyg.parser.node.StrongEmphasis
import me.saket.wysiwyg.parser.node.ThematicBreak
import me.saket.wysiwyg.parser.node.Url
import kotlin.reflect.KClass

class SyntaxHighlighters {

  private val highlighters = mutableMapOf<KClass<out Node>, MutableList<SyntaxHighlighter<*>>>()

  init {
    add(Emphasis::class, EmphasisVisitor())
    add(StrongEmphasis::class, StrongEmphasisVisitor())
    add(LinkWithTitle::class, LinkWithTitleVisitor())
    add(Url::class, UrlVisitor())
    add(Strikethrough::class, StrikethroughVisitor())
    add(Code::class, InlineCodeVisitor())
    add(IndentedCodeBlock::class, IndentedCodeBlockVisitor())
    add(FencedCodeBlock::class, FencedCodeBlockVisitor())
    add(BlockQuote::class, BlockQuoteVisitor())
    add(OrderedList::class, OrderedListVisitor())
    add(BulletList::class, BulletListVisitor())
    add(OrderedListItem::class, OrderedListItemVisitor())
    add(BulletListItem::class, BulletListItemVisitor())
    add(ThematicBreak::class, ThematicBreakVisitor())
    add(Heading::class, HeadingVisitor())
  }

  /**
   * Because multiple [SyntaxHighlighter] could be present for the same [node] and
   * [SyntaxHighlighter] are allowed to have a missing visitor, this tries finds
   * the first NodeVisitor that can read [node].
   */
  fun nodeVisitor(node: Node): NodeVisitor<Node> {
    @Suppress("UNCHECKED_CAST")
    val nodeHighlighters = highlighters[node::class] as List<SyntaxHighlighter<Node>>?

    if (nodeHighlighters != null) {
      // Intentionally using for-i loop instead of for-each or
      // anything else that creates a new Iterator under the hood.
      @Suppress("ReplaceManualRangeWithIndicesCalls")
      for (i in 0 until nodeHighlighters.size) {
        val nodeVisitor = nodeHighlighters[i].visitor(node)
        if (nodeVisitor != null) {
          return nodeVisitor
        }
      }
    }

    return NodeVisitor.EMPTY
  }

  fun <T : Node> add(
    nodeType: KClass<T>,
    visitor: NodeVisitor<T>
  ) =
    add(nodeType, object : SyntaxHighlighter<T> {
      override fun visitor(node: T) = visitor
    })

  fun <T : Node> add(
    nodeType: KClass<T>,
    highlighter: SyntaxHighlighter<T>
  ) {
    if (nodeType in highlighters) {
      highlighters[nodeType]!!.add(highlighter)
    } else {
      @Suppress("ReplacePutWithAssignment") // `highlighters[key] = value` doesn't compile.
      highlighters.put(nodeType, mutableListOf(highlighter))
    }
  }
}
