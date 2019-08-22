package me.saket.wysiwyg.parser.node

expect abstract class Node
expect val Node.firstChild: Node?
expect val Node.nextNode: Node?
expect val Node.startOffset: Int
expect val Node.endOffset: Int

expect interface DelimitedNode
expect val DelimitedNode.openingMarker: CharSequence
expect val DelimitedNode.closingMarker: CharSequence

expect abstract class DelimitedNodeImpl : Node, DelimitedNode

expect class Emphasis : DelimitedNodeImpl
expect val Emphasis.startOffset: Int
expect val Emphasis.endOffset: Int

expect class StrongEmphasis : DelimitedNodeImpl
expect val StrongEmphasis.startOffset: Int
expect val StrongEmphasis.endOffset: Int
