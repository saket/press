package me.saket.wysiwyg.formatting

import kotlin.test.Test

class StrikethroughSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position`() {
    StrikethroughSyntaxApplier.test(
        input = "He was trying to kill ▮ millions of innocent people.",
        output = "He was trying to kill ~~▮~~ millions of innocent people."
    )
  }

  @Test fun `apply to selection`() {
    StrikethroughSyntaxApplier.test(
        input = "Innocent is a ▮strong▮ word to throw around Gotham, Bruce.",
        output = "Innocent is a ▮~~strong~~▮ word to throw around Gotham, Bruce."
    )
  }
}
