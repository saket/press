package me.saket.wysiwyg.spans

expect fun SpanPool.foregroundColor(color: Int): WysiwygSpan
expect fun SpanPool.italics(): WysiwygSpan
expect fun SpanPool.bold(): WysiwygSpan
expect fun SpanPool.strikethrough(): WysiwygSpan