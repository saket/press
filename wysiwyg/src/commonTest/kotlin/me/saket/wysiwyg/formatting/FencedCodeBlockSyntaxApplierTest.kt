package me.saket.wysiwyg.formatting

import kotlin.test.Test

class FencedCodeBlockSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position at the end of the first line in a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?▮
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin(),
        output = """
                |```
                |Gordon: What's your name son?▮
                |```
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of a line in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?
                |John Blake: Blake sir.▮
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin(),
        output = """
                |Gordon: What's your name son?
                |```
                |John Blake: Blake sir.▮
                |```
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of the last line in a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?▮
                """.trimMargin(),
        output = """
                |Gordon: What's your name son?
                |John Blake: Blake sir.
                |```
                |Gordon: You have something you wanna ask me Officer Blake?▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |▮
                """.trimMargin(),
        output = """
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content with leading new line`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |
                |▮
                """.trimMargin(),
        output = """
                |
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |You have something you ▮ wanna ask me Officer Blake?
                """.trimMargin(),
        output = """
                |```
                |You have something you ▮ wanna ask me Officer Blake?
                |```
                """.trimMargin()
    )
  }

  @Test fun `apply to selection in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |You have something you wanna ▮ask me▮ Officer Blake?
                """.trimMargin(),
        output = """
                |```
                |You have something you wanna ▮ask me▮ Officer Blake?
                |```
                """.trimMargin()
    )
  }

  @Test fun `apply to selection to a whole paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |▮You have something you wanna ask me Officer Blake?▮
                """.trimMargin(),
        output = """
                |```
                |▮You have something you wanna ask me Officer Blake?▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |What's your name son?
                |▮
                """.trimMargin(),
        output = """
                |What's your name son?
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line with leading spaces`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |What's your name son?
                |  ▮
                """.trimMargin(),
        output = """
                |What's your name son?
                |```
                |  ▮
                |```
                """.trimMargin()
    )
  }
}
