package me.saket.wysiwyg.parser.node

actual abstract class Node
actual val Node.firstChild: Node? get() = TODO()
actual val Node.nextNode: Node? get() = TODO()
actual val Node.startOffset: Int get() = TODO()
actual val Node.endOffset: Int get() = TODO()

actual interface DelimitedNode
actual val DelimitedNode.openingMarker: CharSequence get() = TODO()
actual val DelimitedNode.closingMarker: CharSequence get() = TODO()

actual abstract class DelimitedNodeImpl : Node(), DelimitedNode

actual class Emphasis : DelimitedNodeImpl()

actual class StrongEmphasis : DelimitedNodeImpl()

actual abstract class LinkNodeBase : Node()
actual abstract class LinkNode : LinkNodeBase()
actual abstract class InlineLinkNode : LinkNode()
actual open class DelimitedLinkNode : LinkNode()

actual class LinkWithTitle : InlineLinkNode()
actual val LinkWithTitle.text: CharSequence get() = TODO()
actual val LinkWithTitle.url: CharSequence get() = TODO()

actual class Url : DelimitedLinkNode()
actual val Url.url: CharSequence get() = TODO()

actual class Strikethrough : Node(), DelimitedNode

actual class Code : DelimitedNodeImpl()

actual abstract class ContentNode : Node()
actual abstract class Block : ContentNode()
actual class IndentedCodeBlock : Block()

actual class FencedCodeBlock : Block()
actual val FencedCodeBlock.openingMarker: CharSequence get() = TODO()
actual val FencedCodeBlock.closingMarker: CharSequence get() = TODO()

actual class BlockQuote : Block()
actual val BlockQuote.parent: Node? get() = TODO()

actual abstract class ListBlock : Block()
actual class OrderedList : ListBlock()
actual class BulletList : ListBlock()

actual abstract class ListItem : Block()
actual val ListItem.openingMarker: CharSequence get() = TODO()

actual class OrderedListItem : ListItem()
actual class BulletListItem : ListItem()

actual class ThematicBreak : Block()
actual val ThematicBreak.chars: CharSequence get() = TODO()

actual class Heading : Block()
actual val Heading.isAtxHeading: Boolean get() = TODO()
actual val Heading.headingLevel: HeadingLevel get() = TODO()
actual val Heading.openingMarker: CharSequence get() = TODO()
