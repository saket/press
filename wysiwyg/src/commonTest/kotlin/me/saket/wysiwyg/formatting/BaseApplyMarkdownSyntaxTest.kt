package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo

abstract class BaseApplyMarkdownSyntaxTest : BaseTextSelectionTest() {

  protected fun MarkdownSyntaxApplier.test(
    input: String,
    output: String
  ) {
    val (parsedText, parsedSelection) = decodeSelection(input)
    val actualFormat = apply(parsedText, parsedSelection)

    val expectedFormat = decodeSelection(output)
    if (actualFormat != expectedFormat) {
      printDifference(expectedFormat, actualFormat)
    }
    assertThat(actualFormat).isEqualTo(expectedFormat)
  }

  protected fun AutoFormatOnEnterPress.onEnterTest(
    input: String,
    output: String?
  ) {
    val (parsedText, parsedSelection) = decodeSelection(input)
    val actualFormat = onEnter(parsedText, parsedSelection)

    val expectedFormat = output?.let(::decodeSelection)
    if (actualFormat != expectedFormat) {
      printDifference(expectedFormat, actualFormat)
    }
    assertThat(actualFormat).isEqualTo(expectedFormat)
  }

  private fun printDifference(
    expected: ApplyMarkdownSyntax?,
    actual: ApplyMarkdownSyntax?
  ) {
    println("--------------------------------------")
    println("Text doesn't match.")
    if (expected != null) {
      println("Expected:\n\"\"\"\n${encodeSelection(expected.newText, expected.newSelection)}\n\"\"\"")
    } else {
      println("Expected: \nnull")
    }
    if (actual != null) {
      println("\nActual: \n\"\"\"\n${encodeSelection(actual.newText, actual.newSelection)}\n\"\"\"")
    } else {
      println("\nActual: \nnull")
    }
  }
}
