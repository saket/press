package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo

abstract class BaseMarkdownSyntaxApplierTest : BaseTextSelectionTest() {

  protected fun MarkdownSyntaxApplier.test(
    input: String,
    output: String
  ) {
    val (parsedText, parsedSelection) = decodeSelection(input)
    val applyFormat = apply(parsedText, parsedSelection)

    val (expectedText, expectedSelection) = decodeSelection(output)
    val expectedApply = ApplyMarkdownSyntax(expectedText, expectedSelection)

    if (applyFormat != expectedApply) {
      println("--------------------------------------")
      println("Text doesn't match.")
      println("Expected:\n'${encodeSelection(expectedText, expectedSelection)}'")
      println("\nActual: \n'${encodeSelection(applyFormat.newText, applyFormat.newSelection)}'")
    }
    assertThat(applyFormat).isEqualTo(expectedApply)
  }
}
