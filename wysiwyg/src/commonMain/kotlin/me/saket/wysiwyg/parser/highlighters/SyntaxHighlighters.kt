package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.node.Emphasis
import me.saket.wysiwyg.parser.node.Node
import kotlin.reflect.KClass

class SyntaxHighlighters {

  private val highlighters = mutableMapOf<KClass<out Node>, MutableList<SyntaxHighlighter<*>>>()

  init {
    add(Emphasis::class, EmphasisVisitor())
    //add(StrongEmphasis::class.java, StrongEmphasisVisitor())
    //add(Link::class.java, LinkVisitor())
    //add(Strikethrough::class.java, StrikethroughVisitor())
    //add(Code::class.java, InlineCodeVisitor())
    //add(IndentedCodeBlock::class.java, IndentedCodeBlockVisitor())
    //add(BlockQuote::class.java, BlockQuoteVisitor())
    //add(ListBlock::class.java, ListBlockVisitor())
    //add(ListItem::class.java, ListItemVisitor())
    //add(ThematicBreak::class.java, ThematicBreakVisitor())
    //add(Heading::class.java, HeadingVisitor())
    //add(FencedCodeBlock::class.java, FencedCodeBlockVisitor())
  }

  /**
   * Because multiple [SyntaxHighlighter] could be present for the same [node] and
   * [SyntaxHighlighter] are allowed to have a missing visitor, this tries finds
   * the first NodeVisitor that can read [node].
   */
  @Suppress("UNCHECKED_CAST")
  fun nodeVisitor(node: Node): NodeVisitor<Node> {
    val nodeHighlighters = highlighters[node::class] as List<SyntaxHighlighter<Node>>?

    if (nodeHighlighters != null) {
      // Intentionally using for-i loop instead of for-each or
      // anything else that creates a new Iterator under the hood.
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
  ) {
    add(
        nodeType = nodeType,
        highlighter = object : SyntaxHighlighter<T> {
          override fun visitor(node: T) = visitor
        }
    )
  }

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