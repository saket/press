package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StrongEmphasisSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test override fun `insert at cursor position`() {
    val (text, selection) = buildSelection(
        "You don't ▮ these people anymore, you've given them everything."
    )
    val applyFormat = StrongEmphasisSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "You don't **▮** these people anymore, you've given them everything."
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }

  @Test override fun `apply to selection`() {
    val (text, selection) = buildSelection("Not everything, ▮not yet▮.")
    val applyFormat = StrongEmphasisSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "Not everything, **not yet**▮."
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }
}
