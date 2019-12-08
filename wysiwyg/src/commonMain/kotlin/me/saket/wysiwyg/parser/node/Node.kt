package me.saket.wysiwyg.parser.node

expect abstract class Node
expect val Node.firstChild: Node?
expect val Node.nextNode: Node?
expect val Node.startOffset: Int
expect val Node.endOffset: Int

expect interface DelimitedNode
expect val DelimitedNode.openingMarker: CharSequence
expect val DelimitedNode.closingMarker: CharSequence

// https://youtrack.jetbrains.net/issue/KT-20641
expect abstract class DelimitedNodeImpl : Node, DelimitedNode

expect class Emphasis : DelimitedNodeImpl

expect class StrongEmphasis : DelimitedNodeImpl

// https://youtrack.jetbrains.net/issue/KT-20641
expect abstract class LinkNodeBase : Node
expect abstract class LinkNode : LinkNodeBase
expect abstract class InlineLinkNode : LinkNode
expect open class DelimitedLinkNode : LinkNode

/**
 * [title](http://example.com)
 */
expect class LinkWithTitle : InlineLinkNode
expect val LinkWithTitle.text: CharSequence

/**
 * http://example.com
 */
expect class Url : DelimitedLinkNode

expect class Strikethrough : Node, DelimitedNode

expect class Code : DelimitedNodeImpl

// https://youtrack.jetbrains.net/issue/KT-20641
expect abstract class ContentNode : Node
expect abstract class Block : ContentNode
expect class IndentedCodeBlock : Block

expect class FencedCodeBlock : Block
expect val FencedCodeBlock.openingMarker: CharSequence
expect val FencedCodeBlock.closingMarker: CharSequence

expect class BlockQuote : Block
expect val BlockQuote.parent: Node?

expect abstract class ListBlock : Block
expect class OrderedList : ListBlock
expect class BulletList : ListBlock

expect abstract class ListItem : Block
expect class OrderedListItem : ListItem
expect class BulletListItem : ListItem

expect class ThematicBreak : Block
expect val ThematicBreak.chars: CharSequence

expect class Heading : Block
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
expect val Heading.isAtxHeading: Boolean
expect val Heading.headingLevel: HeadingLevel
expect val Heading.openingMarker: CharSequence
