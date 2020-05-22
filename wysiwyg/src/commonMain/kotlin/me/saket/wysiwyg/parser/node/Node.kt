package me.saket.wysiwyg.parser.node

interface Node {
  val parent: Node?
  val firstChild: Node?
  val nextNode: Node?
  val startOffset: Int
  val endOffset: Int
}

//expect abstract class Node
//expect val Node.parent: Node?
//expect val Node.firstChild: Node?
//expect val Node.nextNode: Node?
//expect val Node.startOffset: Int
//expect val Node.endOffset: Int

interface DelimitedNode : Node {
  val openingMarker: CharSequence
  val closingMarker: CharSequence
}

//expect interface DelimitedNode
//expect val DelimitedNode.openingMarker: CharSequence
//expect val DelimitedNode.closingMarker: CharSequence

interface Emphasis : DelimitedNode
interface StrongEmphasis : DelimitedNode

// https://youtrack.jetbrains.net/issue/KT-20641
//expect abstract class DelimitedNodeImpl : Node, DelimitedNode
//expect class Emphasis : DelimitedNodeImpl
//expect class StrongEmphasis : DelimitedNodeImpl

interface InlineLinkNode : Node
interface DelimitedLinkNode : Node

// https://youtrack.jetbrains.net/issue/KT-20641
//expect abstract class LinkNodeBase : Node
//expect abstract class LinkNode : LinkNodeBase
//expect abstract class InlineLinkNode : LinkNode
//expect open class DelimitedLinkNode : LinkNode

/**
 * [title](http://example.com)
 */
interface LinkWithTitle : InlineLinkNode {
  val text: CharSequence
  val url: CharSequence
}

//expect class LinkWithTitle : InlineLinkNode
//expect val LinkWithTitle.text: CharSequence
//expect val LinkWithTitle.url: CharSequence

/**
 * http://example.com
 */
interface Url : DelimitedLinkNode {
  val url: CharSequence
}

//expect class Url : DelimitedLinkNode
//expect val Url.url: CharSequence

interface Strikethrough : DelimitedNode

//expect class Strikethrough : Node, DelimitedNode

interface Code : DelimitedNode

//expect class Code : DelimitedNodeImpl

interface IndentedCodeBlock : Node
interface FencedCodeBlock : DelimitedNode

// https://youtrack.jetbrains.net/issue/KT-20641
//expect abstract class ContentNode : Node
//expect abstract class Block : ContentNode
//expect class IndentedCodeBlock : Block

//expect class FencedCodeBlock : Block
//expect val FencedCodeBlock.openingMarker: CharSequence
//expect val FencedCodeBlock.closingMarker: CharSequence

interface BlockQuote : Node

//expect class BlockQuote : Block
//expect val BlockQuote.parent: Node?

interface ListBlock : Node
interface OrderedList : ListBlock
interface BulletList : ListBlock

//expect abstract class ListBlock : Block
//expect class OrderedList : ListBlock
//expect class BulletList : ListBlock

interface ListItem : Node {
  val openingMarker: CharSequence
}
interface OrderedListItem : ListItem
interface BulletListItem : ListItem

//expect abstract class ListItem : Block

//expect val ListItem.openingMarker: CharSequence

//expect class OrderedListItem : ListItem
//expect class BulletListItem : ListItem

interface ThematicBreak: Node {
  val chars: CharSequence
}

//expect class ThematicBreak : Block
//expect val ThematicBreak.chars: CharSequence

interface Heading: Node {
  /**
   * Setext-style headers are "underlined" using "=" for H1
   * and "-" for H2. For example:
   *
   * This is an H1
   * =============
   *
   * This is an H2
   * -------------
   */
  val isAtxHeading: Boolean
  val headingLevel: HeadingLevel
  val openingMarker: CharSequence
}

//expect class Heading : Block
