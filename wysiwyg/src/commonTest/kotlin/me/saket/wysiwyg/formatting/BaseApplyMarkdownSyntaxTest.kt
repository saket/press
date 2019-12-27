package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.saket.wysiwyg.formatting.ReplaceNewLineWith.DeleteLetters
import me.saket.wysiwyg.formatting.ReplaceNewLineWith.InsertLetters

abstract class BaseApplyMarkdownSyntaxTest : BaseTextSelectionTest() {

  protected fun MarkdownSyntaxApplier.test(
    input: String,
    expect: String
  ) {
    val (inputText, inputSelection) = decodeSelection(input)
    val outputFormat: ApplyMarkdownSyntax = apply(inputText, inputSelection)

    val expectedFormat = decodeSelection(expect)
    val expectedText = expectedFormat.first
    val expectedSelection = expectedFormat.second

    if (outputFormat.newText != expectedText || outputFormat.newSelection != expectedSelection) {
      printDifference(
          expectedText = expectedText,
          expectedSelection = expectedSelection,
          actualText = outputFormat.newText,
          actualSelection = outputFormat.newSelection
      )
    }
    assertThat(outputFormat.newText).isEqualTo(expectedText)
    assertThat(outputFormat.newSelection).isEqualTo(expectedSelection)
  }

  protected fun AutoFormatOnEnterPress.onEnterTest(
    input: String,
    expect: String?
  ) {
    val (inputText, inputSelection) = decodeSelection(input)
    val enterReplacement = onEnter(inputText, inputSelection)

    val outputTextAfterFormatting = when (enterReplacement) {
      is InsertLetters -> inputText.replaceRange(
          startIndex = inputSelection.cursorPosition,
          endIndex = inputSelection.cursorPosition,
          replacement = enterReplacement.replacement
      )
      is DeleteLetters -> inputText.replaceRange(
          startIndex = inputSelection.cursorPosition - enterReplacement.deleteCount,
          endIndex = inputSelection.cursorPosition,
          replacement = ""
      )
      null -> null
    }

    val outputSelectionAfterFormatting = when (enterReplacement) {
      is InsertLetters -> enterReplacement.newSelection
      is DeleteLetters -> inputSelection.offsetBy(-enterReplacement.deleteCount)
      null -> null
    }

    val expectedFormat = expect?.let(::decodeSelection)
    val expectedText = expectedFormat?.first
    val expectedSelection = expectedFormat?.second

    if (outputTextAfterFormatting != expectedText || outputSelectionAfterFormatting != expectedSelection) {
      printDifference(
          expectedText = expectedText,
          expectedSelection = expectedSelection,
          actualText = outputTextAfterFormatting?.toString(),
          actualSelection = outputSelectionAfterFormatting
      )
    }
    assertThat(outputTextAfterFormatting).isEqualTo(expectedText)
    assertThat(outputSelectionAfterFormatting).isEqualTo(expectedSelection)
  }

  private fun printDifference(
    expectedText: String?,
    expectedSelection: TextSelection?,
    actualText: String?,
    actualSelection: TextSelection?
  ) {
    println("--------------------------------------")
    println("Text doesn't match.")
    if (expectedText != null && expectedSelection != null) {
      println("Expected:\n\"\"\"\n${encodeSelection(expectedText, expectedSelection)}\n\"\"\"")
    } else {
      println("Expected: \nnull")
    }
    if (actualText != null && actualSelection != null) {
      println("\nActual: \n\"\"\"\n${encodeSelection(actualText, actualSelection)}\n\"\"\"")
    } else {
      println("\nActual: \nnull")
    }
  }
}
