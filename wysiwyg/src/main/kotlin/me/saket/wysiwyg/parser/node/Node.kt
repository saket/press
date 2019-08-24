@file:Suppress("ConflictingExtensionProperty")

package me.saket.wysiwyg.parser.node

import com.vladsch.flexmark.ast.DelimitedNode as FlexmarkDelimitedNode
import com.vladsch.flexmark.ast.DelimitedNodeImpl as FlexmarkDelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis as FlexmarkEmphasis
import com.vladsch.flexmark.ast.InlineLinkNode as FlexmarkInlineLinkNode
import com.vladsch.flexmark.ast.Link as FlexmarkLink
import com.vladsch.flexmark.ast.LinkNode as FlexmarkLinkNode
import com.vladsch.flexmark.ast.LinkNodeBase as FlexmarkLinkNodeBase
import com.vladsch.flexmark.ast.StrongEmphasis as FlexmarkStrongEmphasis
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough as FlexmarkStrikethrough
import com.vladsch.flexmark.util.ast.Node as FlexmarkNode

actual typealias Node = FlexmarkNode
actual val Node.firstChild: Node? get() = firstChild
actual val Node.nextNode: Node? get() = next
actual val Node.startOffset: Int get() = startOffset
actual val Node.endOffset: Int get() = endOffset

actual typealias DelimitedNode = FlexmarkDelimitedNode
actual val DelimitedNode.openingMarker: CharSequence get() = openingMarker
actual val DelimitedNode.closingMarker: CharSequence get() = closingMarker

actual typealias DelimitedNodeImpl = FlexmarkDelimitedNodeImpl

actual typealias Emphasis = FlexmarkEmphasis

actual typealias StrongEmphasis = FlexmarkStrongEmphasis

actual typealias LinkNodeBase = FlexmarkLinkNodeBase
actual typealias LinkNode = FlexmarkLinkNode
actual typealias InlineLinkNode = FlexmarkInlineLinkNode

actual typealias Link = FlexmarkLink
actual val Link.text: CharSequence get() = text

actual typealias Strikethrough = FlexmarkStrikethrough