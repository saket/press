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
