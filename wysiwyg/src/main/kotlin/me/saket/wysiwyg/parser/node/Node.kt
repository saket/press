@file:Suppress("ConflictingExtensionProperty")

package me.saket.wysiwyg.parser.node

import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.parser.node.HeadingLevel.H2
import me.saket.wysiwyg.parser.node.HeadingLevel.H3
import me.saket.wysiwyg.parser.node.HeadingLevel.H4
import me.saket.wysiwyg.parser.node.HeadingLevel.H5
import me.saket.wysiwyg.parser.node.HeadingLevel.H6
import com.vladsch.flexmark.ast.AutoLink as FlexmarkAutoLink
import com.vladsch.flexmark.ast.BlockQuote as FlexmarkBlockQuote
import com.vladsch.flexmark.ast.BulletList as FlexmarkBulletList
import com.vladsch.flexmark.ast.BulletListItem as FlexmarkBulletListItem
import com.vladsch.flexmark.ast.Code as FlexmarkCode
import com.vladsch.flexmark.ast.DelimitedLinkNode as FlexmarkDelimitedLinkNode
import com.vladsch.flexmark.ast.DelimitedNode as FlexmarkDelimitedNode
import com.vladsch.flexmark.ast.DelimitedNodeImpl as FlexmarkDelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis as FlexmarkEmphasis
import com.vladsch.flexmark.ast.FencedCodeBlock as FlexmarkFencedCodeBlock
import com.vladsch.flexmark.ast.Heading as FlexmarkHeading
import com.vladsch.flexmark.ast.IndentedCodeBlock as FlexmarkIndentedCodeBlock
import com.vladsch.flexmark.ast.InlineLinkNode as FlexmarkInlineLinkNode
import com.vladsch.flexmark.ast.Link as FlexmarkLink
import com.vladsch.flexmark.ast.LinkNode as FlexmarkLinkNode
import com.vladsch.flexmark.ast.LinkNodeBase as FlexmarkLinkNodeBase
import com.vladsch.flexmark.ast.ListBlock as FlexmarkListBlock
import com.vladsch.flexmark.ast.ListItem as FlexmarkListItem
import com.vladsch.flexmark.ast.OrderedList as FlexmarkOrderedList
import com.vladsch.flexmark.ast.OrderedListItem as FlexmarkOrderedListItem
import com.vladsch.flexmark.ast.Paragraph as FlexmarkParagraph
import com.vladsch.flexmark.ast.StrongEmphasis as FlexmarkStrongEmphasis
import com.vladsch.flexmark.ast.ThematicBreak as FlexmarkThematicBreak
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough as FlexmarkStrikethrough
import com.vladsch.flexmark.util.ast.Block as FlexmarkBlock
import com.vladsch.flexmark.util.ast.ContentNode as FlexmarkContentNode
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
actual typealias DelimitedLinkNode = FlexmarkDelimitedLinkNode

actual typealias LinkWithTitle = FlexmarkLink

actual val LinkWithTitle.text: CharSequence get() = text

actual typealias Url = FlexmarkAutoLink

actual typealias Strikethrough = FlexmarkStrikethrough

actual typealias Code = FlexmarkCode

actual typealias ContentNode = FlexmarkContentNode
actual typealias Block = FlexmarkBlock
actual typealias IndentedCodeBlock = FlexmarkIndentedCodeBlock

actual typealias FencedCodeBlock = FlexmarkFencedCodeBlock

actual val FencedCodeBlock.openingMarker: CharSequence get() = openingMarker
actual val FencedCodeBlock.closingMarker: CharSequence get() = closingMarker

actual typealias BlockQuote = FlexmarkBlockQuote

actual val BlockQuote.parent: Node? get() = parent

actual typealias ListBlock = FlexmarkListBlock
actual typealias OrderedList = FlexmarkOrderedList
actual typealias BulletList = FlexmarkBulletList

actual typealias ListItem = FlexmarkListItem
actual typealias OrderedListItem = FlexmarkOrderedListItem
actual typealias BulletListItem = FlexmarkBulletListItem

actual typealias ThematicBreak = FlexmarkThematicBreak

actual val ThematicBreak.chars: CharSequence get() = chars

actual typealias Heading = FlexmarkHeading

actual val Heading.headingLevel: HeadingLevel
  get() = when (level) {
    1 -> H1
    2 -> H2
    3 -> H3
    4 -> H4
    5 -> H5
    6 -> H6
    else -> throw AssertionError("Unknown headingLevel: $level")
  }
actual val Heading.isAtxHeading: Boolean get() = isAtxHeading
actual val Heading.openingMarker: CharSequence get() = openingMarker

actual typealias Paragraph = FlexmarkParagraph
