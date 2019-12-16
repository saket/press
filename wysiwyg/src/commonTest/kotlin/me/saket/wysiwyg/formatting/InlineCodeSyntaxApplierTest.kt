package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class InlineCodeSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test override fun `insert at cursor position`() {
    val (text, selection) = buildSelection(
        "This is the ▮ exchange. There's no money you can steal!"
    )
    val applyFormat = InlineCodeSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "This is the `▮` exchange. There's no money you can steal!"
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }

  @Test override fun `apply to selection`() {
    val (text, selection) = buildSelection(
        "Really? Then, why are you ▮people▮ in here?"
    )
    val applyFormat = InlineCodeSyntaxApplier.apply(text, selection)

    val (expectedText, expectedSelection) = buildSelection(
        "Really? Then, why are you `people`▮ in here?"
    )
    assertThat(applyFormat).isEqualTo(ApplyMarkdownSyntax(expectedText, expectedSelection))
  }
}
