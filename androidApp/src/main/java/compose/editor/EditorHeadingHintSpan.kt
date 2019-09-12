package compose.editor

import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.spans.HeadingSpan

class EditorHeadingHintSpan(level: HeadingLevel) : HeadingSpan(recycler = {}) {
  init {
    super.level = level
  }
}