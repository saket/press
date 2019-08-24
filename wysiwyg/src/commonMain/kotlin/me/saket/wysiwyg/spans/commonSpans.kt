package me.saket.wysiwyg.spans

expect fun SpanPool.foregroundColor(color: Int): WysiwygSpan
expect fun SpanPool.italics(): WysiwygSpan
expect fun SpanPool.bold(): WysiwygSpan
expect fun SpanPool.strikethrough(): WysiwygSpan
expect fun SpanPool.inlineCode(): WysiwygSpan
expect fun SpanPool.monospaceTypeface(): WysiwygSpan
expect fun SpanPool.indentedCodeBlock(): WysiwygSpan
expect fun SpanPool.quote(): WysiwygSpan
expect fun SpanPool.leadingMargin(margin: Int): WysiwygSpan