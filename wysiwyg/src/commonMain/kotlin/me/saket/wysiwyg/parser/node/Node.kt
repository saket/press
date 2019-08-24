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

expect class Link : InlineLinkNode
expect val Link.text: CharSequence

expect class Strikethrough : Node, DelimitedNode

expect class Code : DelimitedNodeImpl