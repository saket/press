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
actual val Emphasis.startOffset: Int get() = TODO()
actual val Emphasis.endOffset: Int get() = TODO()

actual class StrongEmphasis : DelimitedNodeImpl()
actual val StrongEmphasis.startOffset: Int get() = TODO()
actual val StrongEmphasis.endOffset: Int get() = TODO()

actual abstract class LinkNodeBase : Node()
actual abstract class LinkNode : LinkNodeBase()
actual abstract class InlineLinkNode : LinkNode()

actual class Link : InlineLinkNode()
actual val Link.text: CharSequence get() = TODO()
actual val Link.startOffset: Int get() = TODO()
actual val Link.endOffset: Int get() = TODO()
