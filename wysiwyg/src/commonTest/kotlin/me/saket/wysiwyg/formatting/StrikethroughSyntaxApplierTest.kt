package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StrikethroughSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test override fun `insert at cursor position`() {
    val (text, selection) = buildSelection(
        "He was trying to kill ▮ millions of innocent people."
    )
    val applyFormat = StrikethroughSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "He was trying to kill ~~▮~~ millions of innocent people."
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }

  @Test override fun `apply to selection`() {
    val (text, selection) = buildSelection(
        "Innocent is a ▮strong▮ word to throw around Gotham, Bruce."
    )
    val applyFormat = StrikethroughSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "Innocent is a ~~strong~~▮ word to throw around Gotham, Bruce."
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }
}
