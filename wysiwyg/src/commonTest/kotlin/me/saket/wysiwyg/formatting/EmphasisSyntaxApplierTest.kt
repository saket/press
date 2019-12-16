package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class EmphasisSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test override fun `insert at cursor position`() {
    val (text, selection) = buildSelection(
        "You think ▮ can last?"
    )
    val applyFormat = EmphasisSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "You think *▮* can last?"
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }

  @Test override fun `apply to selection`() {
    val (text, selection) = buildSelection("" +
        "You think ▮this▮ can last?"
    )
    val applyFormat = EmphasisSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "You think *this*▮ can last?"
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }
}
