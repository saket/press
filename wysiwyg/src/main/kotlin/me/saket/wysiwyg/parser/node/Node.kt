@file:Suppress("ConflictingExtensionProperty")

package me.saket.wysiwyg.parser.node

import com.vladsch.flexmark.ast.DelimitedNode as FlexmarkDelimitedNode
import com.vladsch.flexmark.ast.DelimitedNodeImpl as FlexmarkDelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis as FlexmarkEmphasis
import com.vladsch.flexmark.util.ast.Node as FlexmarkNode

actual typealias Node = FlexmarkNode
actual val Node.firstChild: Node? get() = firstChild
actual val Node.nextNode: Node get() = next
actual val Node.startOffset: Int get() = startOffset
actual val Node.endOffset: Int get() = endOffset

actual typealias DelimitedNodeImpl = FlexmarkDelimitedNodeImpl

actual typealias Emphasis = FlexmarkEmphasis
actual val Emphasis.startOffset: Int get() = startOffset
actual val Emphasis.endOffset: Int get() = endOffset

actual typealias DelimitedNode = FlexmarkDelimitedNode
actual val DelimitedNode.openingMarker: CharSequence get() = openingMarker
actual val DelimitedNode.closingMarker: CharSequence get() = closingMarker
