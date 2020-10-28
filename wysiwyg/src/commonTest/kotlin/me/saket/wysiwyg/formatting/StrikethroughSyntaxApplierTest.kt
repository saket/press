package me.saket.wysiwyg.formatting

import kotlin.test.Test

class StrikethroughSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position`() {
    StrikethroughSyntaxApplier.test(
      input = "He was trying to kill ▮ millions of innocent people.",
      expect = "He was trying to kill ~~▮~~ millions of innocent people."
    )
  }

  @Test fun `apply to selection`() {
    StrikethroughSyntaxApplier.test(
      input = "Innocent is a ▮strong▮ word to throw around Gotham, Bruce.",
      expect = "Innocent is a ▮~~strong~~▮ word to throw around Gotham, Bruce."
    )
  }
}
