package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo

abstract class BaseApplyMarkdownSyntaxTest : BaseTextSelectionTest() {

  protected fun MarkdownSyntaxApplier.test(
    input: String,
    expect: String
  ) {
    val (inputText, inputSelection) = decodeSelection(input)
    val outputFormat: ReplaceTextWith = apply(inputText, inputSelection).let {
      it.copy(replacement = it.replacement.toString())
    }

    val expectedFormat = decodeSelection(expect)
    val expectedText = expectedFormat.first
    val expectedSelection = expectedFormat.second

    if (outputFormat.replacement != expectedText || outputFormat.newSelection != expectedSelection) {
      printDifference(
        expectedText = expectedText,
        expectedSelection = expectedSelection,
        actualText = outputFormat.replacement.toString(),
        actualSelection = outputFormat.newSelection
      )
    }
    assertThat(outputFormat.replacement).isEqualTo(expectedText)
    assertThat(outputFormat.newSelection).isEqualTo(expectedSelection)
  }

  protected fun AutoFormatOnEnterPress.onEnterTest(
    input: String,
    expect: String?
  ) {
    val (inputText, inputSelection) = decodeSelection(input)
    val textReplacement = onEnter(inputText, inputSelection)?.let {
      it.copy(replacement = it.replacement.toString())
    }
    val outputTextAfterFormatting = textReplacement?.replacement?.toString()
    val outputSelectionAfterFormatting = textReplacement?.newSelection

    val expectedFormat = expect?.let(::decodeSelection)
    val expectedText = expectedFormat?.first
    val expectedSelection = expectedFormat?.second

    if (outputTextAfterFormatting != expectedText || outputSelectionAfterFormatting != expectedSelection) {
      error(printDifference(
        expectedText = expectedText,
        expectedSelection = expectedSelection,
        actualText = outputTextAfterFormatting,
        actualSelection = outputSelectionAfterFormatting
      ))
    }
  }

  private fun printDifference(
    expectedText: String?,
    expectedSelection: TextSelection?,
    actualText: String?,
    actualSelection: TextSelection?
  ) = buildString {
    appendLine("--------------------------------------")
    appendLine("Text doesn't match.")
    if (expectedText != null) {
      appendLine("Expected:\n\"\"\"\n${encodeSelection(expectedText, expectedSelection)}\n\"\"\"")
    } else {
      appendLine("Expected: \nnull")
    }
    if (actualText != null) {
      appendLine("\nActual: \n\"\"\"\n${encodeSelection(actualText, actualSelection)}\n\"\"\"")
    } else {
      appendLine("\nActual: \nnull")
    }
  }
}
